package com.skywings.service;

import com.skywings.dto.request.LoginRequest;
import com.skywings.dto.request.SignupRequest;
import com.skywings.dto.response.AuthResponse;
import com.skywings.entity.User;
import com.skywings.entity.enums.Role;
import com.skywings.exception.DuplicateResourceException;
import com.skywings.exception.OtpVerificationException;
import com.skywings.exception.UnauthorizedException;
import com.skywings.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;

    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("Phone number already registered");
        }

        User user = User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .phone(request.getPhone())
            .role(Role.PASSENGER)
            .emailVerified(false)
            .build();

        user = userRepository.save(user);

        // Send verification OTP to email
        sendVerificationOtp(user.getEmail());

        AuthResponse response = buildAuthResponse(user);
        response.setEmailVerified(false);
        return response;
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        AuthResponse response = buildAuthResponse(user);
        response.setEmailVerified(user.getEmailVerified());
        return response;
    }

    public void sendVerificationOtp(String email) {
        String otp = generateOtp();
        String redisKey = "verify_email:" + email;

        redisTemplate.opsForValue().set(redisKey, otp, 10, TimeUnit.MINUTES);

        try {
            emailService.sendOtpEmail(email, otp);
            log.info("Verification OTP sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send verification OTP to {}: {}", email, e.getMessage());
        }
    }

    public AuthResponse verifyEmail(String email, String otp) {
        String redisKey = "verify_email:" + email;
        String storedOtp = redisTemplate.opsForValue().get(redisKey);

        if (storedOtp == null) {
            throw new OtpVerificationException("OTP expired. Please request a new one.");
        }

        if (!storedOtp.equals(otp)) {
            throw new OtpVerificationException("Invalid OTP. Please try again.");
        }

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("User not found"));

        user.setEmailVerified(true);
        userRepository.save(user);

        redisTemplate.delete(redisKey);
        log.info("Email verified for {}", email);

        AuthResponse response = buildAuthResponse(user);
        response.setEmailVerified(true);
        return response;
    }

    public void resendVerificationOtp(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (user.getEmailVerified()) {
            throw new OtpVerificationException("Email already verified");
        }

        sendVerificationOtp(email);
    }

    public AuthResponse refreshToken(String refreshToken) {
        String email = jwtService.extractEmail(refreshToken);
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        AuthResponse response = buildAuthResponse(user);
        response.setEmailVerified(user.getEmailVerified());
        return response;
    }

    private AuthResponse buildAuthResponse(User user) {
        Map<String, Object> claims = Map.of(
            "userId", user.getId(),
            "role", user.getRole().name()
        );

        String accessToken = jwtService.generateAccessToken(claims, user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .name(user.getName())
            .email(user.getEmail())
            .role(user.getRole().name())
            .emailVerified(user.getEmailVerified())
            .build();
    }

    private String generateOtp() {
        return String.valueOf(100000 + new SecureRandom().nextInt(900000));
    }
}
