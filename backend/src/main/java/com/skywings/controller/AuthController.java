package com.skywings.controller;

import com.skywings.dto.request.LoginRequest;
import com.skywings.dto.request.RefreshTokenRequest;
import com.skywings.dto.request.SignupRequest;
import com.skywings.dto.response.AuthResponse;
import com.skywings.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<AuthResponse> verifyEmail(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String otp = body.get("otp");
        if (email == null || email.isBlank() || otp == null || otp.isBlank()) {
            throw new IllegalArgumentException("Email and OTP are required");
        }
        if (!otp.matches("^\\d{6}$")) {
            throw new IllegalArgumentException("OTP must be a 6-digit number");
        }
        return ResponseEntity.ok(authService.verifyEmail(email.trim(), otp.trim()));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        authService.resendVerificationOtp(email.trim());
        return ResponseEntity.ok(Map.of("message", "Verification OTP sent"));
    }
}
