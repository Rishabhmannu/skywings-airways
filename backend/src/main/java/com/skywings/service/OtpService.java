package com.skywings.service;

import com.skywings.config.OtpStore;
import com.skywings.exception.OtpVerificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpStore otpStore;
    private final TwilioSmsSender twilioSmsSender;
    private final EmailService emailService;

    private static final long OTP_TTL_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 3;
    private static final int MAX_RESENDS = 3;

    public OtpSendResult sendOtp(Long bookingId, String email, String phone) {
        String otp = generateOtp();
        String otpKey = "otp:" + bookingId;
        String attemptKey = "otp_attempts:" + bookingId;

        otpStore.set(otpKey, otp, OTP_TTL_MINUTES, TimeUnit.MINUTES);
        otpStore.set(attemptKey, "0", OTP_TTL_MINUTES, TimeUnit.MINUTES);

        List<String> sentVia = new ArrayList<>();

        try {
            twilioSmsSender.sendOtp(phone, otp);
            sentVia.add("SMS");
            log.info("OTP sent via SMS for booking {}", bookingId);
        } catch (Exception e) {
            log.warn("SMS OTP failed for booking {}: {}", bookingId, e.getMessage());
        }

        try {
            emailService.sendOtpEmail(email, otp);
            sentVia.add("EMAIL");
            log.info("OTP sent via email for booking {}", bookingId);
        } catch (Exception e) {
            log.warn("Email OTP failed for booking {}: {}", bookingId, e.getMessage());
        }

        if (sentVia.isEmpty()) {
            log.warn("Both OTP channels failed for booking {}. OTP for dev: {}", bookingId, otp);
            sentVia.add("DEV_LOG");
        }

        return new OtpSendResult(sentVia, OTP_TTL_MINUTES);
    }

    public boolean verifyOtp(Long bookingId, String inputOtp) {
        String otpKey = "otp:" + bookingId;
        String attemptKey = "otp_attempts:" + bookingId;

        String attempts = otpStore.get(attemptKey);
        if (attempts != null && Integer.parseInt(attempts) >= MAX_ATTEMPTS) {
            otpStore.delete(otpKey);
            otpStore.delete(attemptKey);
            throw new OtpVerificationException("Maximum OTP attempts exceeded. Please request a new OTP.");
        }

        String storedOtp = otpStore.get(otpKey);
        if (storedOtp == null) {
            throw new OtpVerificationException("OTP expired. Please request a new OTP.");
        }

        if (storedOtp.equals(inputOtp)) {
            otpStore.delete(otpKey);
            otpStore.delete(attemptKey);
            return true;
        } else {
            otpStore.increment(attemptKey);
            int used = Integer.parseInt(attempts != null ? attempts : "0") + 1;
            int remaining = MAX_ATTEMPTS - used;
            throw new OtpVerificationException("Invalid OTP. " + remaining + " attempt(s) remaining.");
        }
    }

    public OtpSendResult resendOtp(Long bookingId, String email, String phone, String channel) {
        String resendKey = "otp_resends:" + bookingId;
        String resendCount = otpStore.get(resendKey);

        if (resendCount != null && Integer.parseInt(resendCount) >= MAX_RESENDS) {
            throw new OtpVerificationException("Maximum OTP resends exceeded.");
        }

        otpStore.increment(resendKey);
        otpStore.expire(resendKey, OTP_TTL_MINUTES * 2, TimeUnit.MINUTES);

        return sendOtp(bookingId, email, phone);
    }

    private String generateOtp() {
        return String.valueOf(100000 + new SecureRandom().nextInt(900000));
    }

    public record OtpSendResult(List<String> sentVia, long ttlMinutes) {}
}
