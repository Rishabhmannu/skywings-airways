package com.skywings.service;

import com.skywings.config.OtpStore;
import com.skywings.exception.OtpVerificationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock private OtpStore otpStore;
    @Mock private TwilioSmsSender twilioSmsSender;
    @Mock private EmailService emailService;

    @InjectMocks private OtpService otpService;

    @Test
    void sendOtp_shouldStoreAndSendViaBothChannels() {
        doNothing().when(twilioSmsSender).sendOtp(anyString(), anyString());
        doNothing().when(emailService).sendOtpEmail(anyString(), anyString());

        OtpService.OtpSendResult result = otpService.sendOtp(1L, "r@test.com", "+911234567890");

        assertThat(result.sentVia()).contains("SMS", "EMAIL");
        verify(otpStore).set(eq("otp:1"), anyString(), eq(5L), eq(TimeUnit.MINUTES));
    }

    @Test
    void sendOtp_smsFails_shouldStillSendEmail() {
        doThrow(new RuntimeException("SMS failed")).when(twilioSmsSender).sendOtp(anyString(), anyString());
        doNothing().when(emailService).sendOtpEmail(anyString(), anyString());

        OtpService.OtpSendResult result = otpService.sendOtp(1L, "r@test.com", "+911234567890");

        assertThat(result.sentVia()).contains("EMAIL");
        assertThat(result.sentVia()).doesNotContain("SMS");
    }

    @Test
    void verifyOtp_correctOtp_shouldReturnTrue() {
        when(otpStore.get("otp:1")).thenReturn("123456");
        when(otpStore.get("otp_attempts:1")).thenReturn("0");

        boolean result = otpService.verifyOtp(1L, "123456");

        assertThat(result).isTrue();
        verify(otpStore).delete("otp:1");
        verify(otpStore).delete("otp_attempts:1");
    }

    @Test
    void verifyOtp_wrongOtp_shouldThrowWithRemainingAttempts() {
        when(otpStore.get("otp:1")).thenReturn("123456");
        when(otpStore.get("otp_attempts:1")).thenReturn("0");
        when(otpStore.increment("otp_attempts:1")).thenReturn(1L);

        assertThatThrownBy(() -> otpService.verifyOtp(1L, "999999"))
                .isInstanceOf(OtpVerificationException.class)
                .hasMessageContaining("2 attempt(s) remaining");
    }

    @Test
    void verifyOtp_expiredOtp_shouldThrow() {
        when(otpStore.get("otp_attempts:1")).thenReturn("0");
        when(otpStore.get("otp:1")).thenReturn(null);

        assertThatThrownBy(() -> otpService.verifyOtp(1L, "123456"))
                .isInstanceOf(OtpVerificationException.class)
                .hasMessageContaining("OTP expired");
    }

    @Test
    void verifyOtp_maxAttemptsExceeded_shouldThrow() {
        when(otpStore.get("otp_attempts:1")).thenReturn("3");

        assertThatThrownBy(() -> otpService.verifyOtp(1L, "123456"))
                .isInstanceOf(OtpVerificationException.class)
                .hasMessageContaining("Maximum OTP attempts exceeded");
    }
}
