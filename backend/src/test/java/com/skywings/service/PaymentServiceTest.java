package com.skywings.service;

import com.skywings.dto.request.PaymentRequest;
import com.skywings.dto.response.PaymentResponse;
import com.skywings.entity.Booking;
import com.skywings.entity.Payment;
import com.skywings.entity.User;
import com.skywings.entity.enums.*;
import com.skywings.exception.InvalidPaymentException;
import com.skywings.exception.ResourceNotFoundException;
import com.skywings.repository.BookingRepository;
import com.skywings.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private OtpService otpService;

    @InjectMocks private PaymentService paymentService;

    private User testUser() {
        return User.builder().id(1L).name("Rishabh").email("r@test.com")
                .phone("+911234567890").role(Role.PASSENGER).build();
    }

    @Test
    void initiatePayment_withValidCard_shouldCreatePaymentAndSendOtp() {
        User user = testUser();
        Booking booking = Booking.builder().id(1L).user(user)
                .status(BookingStatus.PENDING).totalPrice(new BigDecimal("13650")).build();

        when(bookingRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBookingId(1L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(otpService.sendOtp(eq(1L), eq("r@test.com"), eq("+911234567890")))
                .thenReturn(new OtpService.OtpSendResult(List.of("EMAIL", "SMS"), 5));

        PaymentRequest request = new PaymentRequest(1L, "4532015112830366", "12/28", "123", "CREDIT_CARD");

        PaymentResponse response = paymentService.initiatePayment(request, user);

        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getCardLastFour()).isEqualTo("0366");
        assertThat(response.getOtpSentVia()).containsExactly("EMAIL", "SMS");
        assertThat(response.getTransactionId()).startsWith("SKY-");
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void initiatePayment_withInvalidCard_shouldThrow() {
        User user = testUser();
        Booking booking = Booking.builder().id(1L).user(user)
                .status(BookingStatus.PENDING).totalPrice(new BigDecimal("13650")).build();

        when(bookingRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(booking));

        PaymentRequest request = new PaymentRequest(1L, "1234567890123456", "12/28", "123", "CREDIT_CARD");

        assertThatThrownBy(() -> paymentService.initiatePayment(request, user))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessage("Invalid card number");
    }

    @Test
    void initiatePayment_withExpiredCard_shouldThrow() {
        User user = testUser();
        Booking booking = Booking.builder().id(1L).user(user)
                .status(BookingStatus.PENDING).totalPrice(new BigDecimal("13650")).build();

        when(bookingRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(booking));

        PaymentRequest request = new PaymentRequest(1L, "4532015112830366", "01/20", "123", "CREDIT_CARD");

        assertThatThrownBy(() -> paymentService.initiatePayment(request, user))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessage("Card has expired");
    }

    @Test
    void initiatePayment_bookingNotPending_shouldThrow() {
        User user = testUser();
        Booking booking = Booking.builder().id(1L).user(user)
                .status(BookingStatus.CONFIRMED).build();

        when(bookingRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(booking));

        PaymentRequest request = new PaymentRequest(1L, "4532015112830366", "12/28", "123", "CREDIT_CARD");

        assertThatThrownBy(() -> paymentService.initiatePayment(request, user))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessageContaining("pending bookings");
    }

    @Test
    void verifyOtp_validOtp_shouldConfirmBooking() {
        User user = testUser();
        Booking booking = Booking.builder().id(1L).user(user)
                .status(BookingStatus.PENDING).build();
        Payment payment = Payment.builder().id(1L).booking(booking)
                .status(PaymentStatus.PENDING).transactionId("SKY-1234-ABCD")
                .amount(new BigDecimal("13650")).paymentMethod(PaymentMethod.CREDIT_CARD)
                .cardLastFour("0366").build();

        when(bookingRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBookingId(1L)).thenReturn(Optional.of(payment));
        when(otpService.verifyOtp(1L, "123456")).thenReturn(true);
        when(paymentRepository.save(any())).thenReturn(payment);
        when(bookingRepository.save(any())).thenReturn(booking);

        PaymentResponse response = paymentService.verifyOtp(1L, "123456", user);

        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(payment.getOtpVerified()).isTrue();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void verifyOtp_bookingNotFound_shouldThrow() {
        when(bookingRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.verifyOtp(999L, "123456", testUser()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
