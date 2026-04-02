package com.skywings.service;

import com.skywings.dto.request.LoginRequest;
import com.skywings.dto.request.SignupRequest;
import com.skywings.dto.response.AuthResponse;
import com.skywings.entity.User;
import com.skywings.entity.enums.Role;
import com.skywings.exception.DuplicateResourceException;
import com.skywings.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private EmailService emailService;
    @Mock private org.springframework.data.redis.core.RedisTemplate<String, String> redisTemplate;
    @Mock private org.springframework.data.redis.core.ValueOperations<String, String> valueOps;

    @InjectMocks private AuthService authService;

    @Test
    void signup_withNewEmail_createsUserAndReturnsTokens() {
        SignupRequest request = new SignupRequest("Rishabh", "r@test.com", "Pass@123", "+911234567890");

        when(userRepository.existsByEmail("r@test.com")).thenReturn(false);
        when(userRepository.existsByPhone("+911234567890")).thenReturn(false);
        when(passwordEncoder.encode("Pass@123")).thenReturn("hashed");
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtService.generateAccessToken(any(Map.class), any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        AuthResponse response = authService.signup(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getName()).isEqualTo("Rishabh");
        assertThat(response.getRole()).isEqualTo("PASSENGER");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signup_withExistingEmail_throwsDuplicateResourceException() {
        SignupRequest request = new SignupRequest("Test", "existing@test.com", "Pass@123", "+911111111111");
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email already registered");
    }

    @Test
    void signup_withExistingPhone_throwsDuplicateResourceException() {
        SignupRequest request = new SignupRequest("Test", "new@test.com", "Pass@123", "+911234567890");
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userRepository.existsByPhone("+911234567890")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Phone number already registered");
    }

    @Test
    void login_withValidCredentials_returnsTokens() {
        LoginRequest request = new LoginRequest("r@test.com", "Pass@123");
        User user = User.builder().id(1L).name("Rishabh").email("r@test.com")
                .passwordHash("hashed").role(Role.PASSENGER).build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("r@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(any(Map.class), any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        AuthResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getEmail()).isEqualTo("r@test.com");
    }
}
