package com.skywings.service;

import com.skywings.dto.request.PaymentRequest;
import com.skywings.dto.response.PaymentResponse;
import com.skywings.entity.Booking;
import com.skywings.entity.Payment;
import com.skywings.entity.User;
import com.skywings.entity.enums.BookingStatus;
import com.skywings.entity.enums.PaymentMethod;
import com.skywings.entity.enums.PaymentStatus;
import com.skywings.exception.InvalidPaymentException;
import com.skywings.exception.ResourceNotFoundException;
import com.skywings.repository.BookingRepository;
import com.skywings.repository.PaymentRepository;
import com.skywings.util.LuhnValidator;
import com.skywings.util.TransactionIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final OtpService otpService;
    private final NotificationService notificationService;

    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request, User user) {
        Booking booking = bookingRepository.findByIdAndUserId(request.getBookingId(), user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new InvalidPaymentException("Payment can only be initiated for pending bookings");
        }

        // Validate card number via Luhn algorithm
        if (!LuhnValidator.isValid(request.getCardNumber())) {
            throw new InvalidPaymentException("Invalid card number");
        }

        // Validate expiry date
        validateExpiryDate(request.getExpiryDate());

        // Check if payment already exists for this booking
        paymentRepository.findByBookingId(booking.getId()).ifPresent(existing -> {
            throw new InvalidPaymentException("Payment already initiated for this booking. " +
                "Transaction ID: " + existing.getTransactionId());
        });

        String transactionId = TransactionIdGenerator.generate();
        String cardLastFour = LuhnValidator.getLastFour(request.getCardNumber());

        Payment payment = Payment.builder()
            .booking(booking)
            .amount(booking.getTotalPrice())
            .paymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()))
            .status(PaymentStatus.PENDING)
            .transactionId(transactionId)
            .cardLastFour(cardLastFour)
            .otpVerified(false)
            .build();
        paymentRepository.save(payment);

        // Send OTP via both channels
        OtpService.OtpSendResult otpResult = otpService.sendOtp(
            booking.getId(), user.getEmail(), user.getPhone());

        log.info("Payment initiated for booking {} — txn: {}", booking.getId(), transactionId);

        return PaymentResponse.builder()
            .transactionId(transactionId)
            .amount(booking.getTotalPrice())
            .paymentMethod(request.getPaymentMethod())
            .cardLastFour(cardLastFour)
            .status("PENDING")
            .otpSentVia(otpResult.sentVia())
            .message("OTP sent. Please verify to complete payment.")
            .build();
    }

    @Transactional
    public PaymentResponse verifyOtp(Long bookingId, String otp, User user) {
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        Payment payment = paymentRepository.findByBookingId(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found for this booking"));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new InvalidPaymentException("Payment already completed");
        }

        // This throws OtpVerificationException if invalid
        otpService.verifyOtp(bookingId, otp);

        // OTP verified — complete payment and confirm booking
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setOtpVerified(true);
        paymentRepository.save(payment);

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPayment(payment);
        bookingRepository.save(booking);

        log.info("Payment completed for booking {} — txn: {}", bookingId, payment.getTransactionId());

        // Send confirmation email AFTER transaction commits (so DB has CONFIRMED status)
        Long confirmedBookingId = booking.getId();
        org.springframework.transaction.support.TransactionSynchronizationManager
            .registerSynchronization(new org.springframework.transaction.support.TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    notificationService.sendBookingConfirmation(confirmedBookingId);
                }
            });

        return PaymentResponse.builder()
            .transactionId(payment.getTransactionId())
            .amount(payment.getAmount())
            .paymentMethod(payment.getPaymentMethod().name())
            .cardLastFour(payment.getCardLastFour())
            .status("COMPLETED")
            .message("Payment verified successfully. Booking confirmed!")
            .build();
    }

    public PaymentResponse resendOtp(Long bookingId, String channel, User user) {
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        Payment payment = paymentRepository.findByBookingId(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new InvalidPaymentException("Payment already completed");
        }

        OtpService.OtpSendResult result = otpService.resendOtp(
            bookingId, user.getEmail(), user.getPhone(), channel);

        return PaymentResponse.builder()
            .transactionId(payment.getTransactionId())
            .status("PENDING")
            .otpSentVia(result.sentVia())
            .message("OTP resent successfully.")
            .build();
    }

    private void validateExpiryDate(String expiryDate) {
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/yy");
            YearMonth expiry = YearMonth.parse(expiryDate, fmt);
            if (expiry.isBefore(YearMonth.now())) {
                throw new InvalidPaymentException("Card has expired");
            }
        } catch (InvalidPaymentException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidPaymentException("Invalid expiry date format. Use MM/YY");
        }
    }
}
