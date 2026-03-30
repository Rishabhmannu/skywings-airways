package com.skywings.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerificationRequest {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotBlank(message = "OTP is required")
    private String otp;
}
