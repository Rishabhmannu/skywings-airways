package com.skywings.service;

import com.skywings.entity.Booking;
import com.skywings.entity.Flight;
import com.skywings.entity.Payment;
import com.skywings.entity.User;
import com.skywings.repository.BookingRepository;
import com.skywings.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailService emailService;
    private final TicketService ticketService;
    private final TemplateEngine templateEngine;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    @Async
    @Transactional(readOnly = true)
    public void sendBookingConfirmation(Long bookingId) {
        try {
            // Fetch fresh from DB (called after transaction commits, so status is CONFIRMED)
            Booking booking = bookingRepository.findById(bookingId).orElse(null);
            if (booking == null) { log.error("Booking {} not found for confirmation email", bookingId); return; }

            Flight flight = booking.getFlight();
            User user = booking.getUser();
            Payment payment = paymentRepository.findByBookingId(booking.getId()).orElse(null);

            Context context = new Context();
            context.setVariable("booking", booking);
            context.setVariable("flight", flight);
            context.setVariable("payment", payment);

            String html = templateEngine.process("booking-confirmation", context);
            byte[] eTicket = ticketService.generateETicketPdf(booking);

            emailService.sendBookingConfirmation(
                user.getEmail(),
                "Booking Confirmed — " + flight.getFlightNumber(),
                html, eTicket);

            log.info("Booking confirmation sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send booking confirmation for booking {}: {}",
                bookingId, e.getMessage(), e);
        }
    }

    @Async
    @Transactional(readOnly = true)
    public void sendCancellationNotice(Long bookingId) {
        try {
            Booking booking = bookingRepository.findById(bookingId).orElse(null);
            if (booking == null) return;

            Flight flight = booking.getFlight();
            User user = booking.getUser();

            Context context = new Context();
            context.setVariable("booking", booking);
            context.setVariable("flight", flight);
            context.setVariable("penalty", booking.getPenaltyAmount());

            String html = templateEngine.process("cancellation-notice", context);

            emailService.sendCancellationNotice(
                user.getEmail(),
                "Booking Cancelled — " + flight.getFlightNumber(),
                html);

            log.info("Cancellation notice sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send cancellation notice for booking {}: {}",
                bookingId, e.getMessage(), e);
        }
    }
}
