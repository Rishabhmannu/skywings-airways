package com.skywings.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skywings.dto.request.LoginRequest;
import com.skywings.dto.request.SignupRequest;
import com.skywings.dto.response.AuthResponse;
import com.skywings.exception.DuplicateResourceException;
import com.skywings.exception.GlobalExceptionHandler;
import com.skywings.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthService authService;
    @MockBean private com.skywings.service.JwtService jwtService;
    @MockBean private com.skywings.config.JwtAuthenticationFilter jwtAuthFilter;
    @MockBean private com.skywings.repository.UserRepository userRepository;

    @Test
    void signup_validRequest_returns201() throws Exception {
        AuthResponse response = AuthResponse.builder()
                .accessToken("token").refreshToken("refresh")
                .name("Rishabh").email("r@test.com").role("PASSENGER").build();

        when(authService.signup(any(SignupRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SignupRequest("Rishabh", "r@test.com", "Pass@123", "+911234567890"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("token"))
                .andExpect(jsonPath("$.name").value("Rishabh"))
                .andExpect(jsonPath("$.role").value("PASSENGER"));
    }

    @Test
    void signup_duplicateEmail_returns409() throws Exception {
        when(authService.signup(any())).thenThrow(new DuplicateResourceException("Email already registered"));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SignupRequest("Test", "dup@test.com", "Pass@123", "+911111111111"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already registered"));
    }

    @Test
    void signup_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SignupRequest("Test", "not-an-email", "Pass@123", "+911111111111"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void login_validCredentials_returns200() throws Exception {
        AuthResponse response = AuthResponse.builder()
                .accessToken("token").refreshToken("refresh")
                .name("Rishabh").email("r@test.com").role("PASSENGER").build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("r@test.com", "Pass@123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token"));
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        when(authService.login(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("r@test.com", "wrong"))))
                .andExpect(status().isUnauthorized());
    }
}
