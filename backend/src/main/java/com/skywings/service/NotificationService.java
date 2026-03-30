package com.skywings.service;

import com.skywings.entity.Booking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailService emailService;
    private final TicketService ticketService;
    private final TemplateEngine templateEngine;

    @Async
    public void sendBookingConfirmation(Booking booking) {
        try {
            Context context = new Context();
            context.setVariable("booking", booking);
            context.setVariable("flight", booking.getFlight());
            context.setVariable("payment", booking.getPayment());

            String html = templateEngine.process("booking-confirmation", context);
            byte[] eTicket = ticketService.generateETicketPdf(booking);

            emailService.sendBookingConfirmation(
                booking.getUser().getEmail(),
                "Booking Confirmed — " + booking.getFlight().getFlightNumber(),
                html, eTicket);

            log.info("Booking confirmation sent to {}", booking.getUser().getEmail());
        } catch (Exception e) {
            log.error("Failed to send booking confirmation for booking {}: {}",
                booking.getId(), e.getMessage());
        }
    }

    @Async
    public void sendCancellationNotice(Booking booking) {
        try {
            Context context = new Context();
            context.setVariable("booking", booking);
            context.setVariable("flight", booking.getFlight());
            context.setVariable("penalty", booking.getPenaltyAmount());

            String html = templateEngine.process("cancellation-notice", context);

            emailService.sendCancellationNotice(
                booking.getUser().getEmail(),
                "Booking Cancelled — " + booking.getFlight().getFlightNumber(),
                html);

            log.info("Cancellation notice sent to {}", booking.getUser().getEmail());
        } catch (Exception e) {
            log.error("Failed to send cancellation notice for booking {}: {}",
                booking.getId(), e.getMessage());
        }
    }
}
