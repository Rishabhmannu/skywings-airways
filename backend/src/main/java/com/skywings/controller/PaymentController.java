package com.skywings.controller;

import com.skywings.dto.request.OtpVerificationRequest;
import com.skywings.dto.request.PaymentRequest;
import com.skywings.dto.response.PaymentResponse;
import com.skywings.entity.User;
import com.skywings.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiatePayment(@Valid @RequestBody PaymentRequest request,
                                                            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(paymentService.initiatePayment(request, user));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<PaymentResponse> verifyOtp(@Valid @RequestBody OtpVerificationRequest request,
                                                      @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(paymentService.verifyOtp(
            request.getBookingId(), request.getOtp(), user));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<PaymentResponse> resendOtp(@RequestBody Map<String, Object> body,
                                                      @AuthenticationPrincipal User user) {
        Long bookingId = Long.valueOf(body.get("bookingId").toString());
        String channel = body.getOrDefault("channel", "BOTH").toString();
        return ResponseEntity.ok(paymentService.resendOtp(bookingId, channel, user));
    }
}
