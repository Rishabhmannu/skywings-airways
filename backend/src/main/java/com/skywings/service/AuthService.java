package com.skywings.service;

import com.skywings.dto.request.LoginRequest;
import com.skywings.dto.request.SignupRequest;
import com.skywings.dto.response.AuthResponse;
import com.skywings.entity.User;
import com.skywings.entity.enums.Role;
import com.skywings.exception.DuplicateResourceException;
import com.skywings.exception.UnauthorizedException;
import com.skywings.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

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
            .build();

        user = userRepository.save(user);
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        return buildAuthResponse(user);
    }

    public AuthResponse refreshToken(String refreshToken) {
        String email = jwtService.extractEmail(refreshToken);
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        return buildAuthResponse(user);
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
            .build();
    }
}
