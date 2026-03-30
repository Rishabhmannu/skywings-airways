package com.skywings.service;

import com.skywings.exception.OtpVerificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final RedisTemplate<String, String> redisTemplate;
    private final TwilioSmsSender twilioSmsSender;
    private final EmailService emailService;

    private static final long OTP_TTL_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 3;
    private static final int MAX_RESENDS = 3;

    public OtpSendResult sendOtp(Long bookingId, String email, String phone) {
        String otp = generateOtp();
        String redisKey = "otp:" + bookingId;
        String attemptKey = "otp_attempts:" + bookingId;
        String resendKey = "otp_resends:" + bookingId;

        redisTemplate.opsForValue().set(redisKey, otp, OTP_TTL_MINUTES, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(attemptKey, "0", OTP_TTL_MINUTES, TimeUnit.MINUTES);

        List<String> sentVia = new ArrayList<>();
        boolean smsSent = false;
        boolean emailSent = false;

        try {
            twilioSmsSender.sendOtp(phone, otp);
            smsSent = true;
            sentVia.add("SMS");
            log.info("OTP sent via SMS for booking {}", bookingId);
        } catch (Exception e) {
            log.warn("SMS OTP failed for booking {}: {}", bookingId, e.getMessage());
        }

        try {
            emailService.sendOtpEmail(email, otp);
            emailSent = true;
            sentVia.add("EMAIL");
            log.info("OTP sent via email for booking {}", bookingId);
        } catch (Exception e) {
            log.warn("Email OTP failed for booking {}: {}", bookingId, e.getMessage());
        }

        if (!smsSent && !emailSent) {
            // Fallback: log OTP for development/testing
            log.warn("Both OTP channels failed for booking {}. OTP for dev: {}", bookingId, otp);
            sentVia.add("DEV_LOG");
        }

        return new OtpSendResult(sentVia, OTP_TTL_MINUTES);
    }

    public boolean verifyOtp(Long bookingId, String inputOtp) {
        String redisKey = "otp:" + bookingId;
        String attemptKey = "otp_attempts:" + bookingId;

        String attempts = redisTemplate.opsForValue().get(attemptKey);
        if (attempts != null && Integer.parseInt(attempts) >= MAX_ATTEMPTS) {
            redisTemplate.delete(redisKey);
            redisTemplate.delete(attemptKey);
            throw new OtpVerificationException("Maximum OTP attempts exceeded. Please request a new OTP.");
        }

        String storedOtp = redisTemplate.opsForValue().get(redisKey);
        if (storedOtp == null) {
            throw new OtpVerificationException("OTP expired. Please request a new OTP.");
        }

        if (storedOtp.equals(inputOtp)) {
            redisTemplate.delete(redisKey);
            redisTemplate.delete(attemptKey);
            return true;
        } else {
            redisTemplate.opsForValue().increment(attemptKey);
            int used = Integer.parseInt(attempts != null ? attempts : "0") + 1;
            int remaining = MAX_ATTEMPTS - used;
            throw new OtpVerificationException("Invalid OTP. " + remaining + " attempt(s) remaining.");
        }
    }

    public OtpSendResult resendOtp(Long bookingId, String email, String phone, String channel) {
        String resendKey = "otp_resends:" + bookingId;
        String resendCount = redisTemplate.opsForValue().get(resendKey);

        if (resendCount != null && Integer.parseInt(resendCount) >= MAX_RESENDS) {
            throw new OtpVerificationException("Maximum OTP resends exceeded.");
        }

        redisTemplate.opsForValue().increment(resendKey);
        redisTemplate.expire(resendKey, OTP_TTL_MINUTES * 2, TimeUnit.MINUTES);

        return sendOtp(bookingId, email, phone);
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public record OtpSendResult(List<String> sentVia, long ttlMinutes) {}
}
