package com.skywings.controller;

import com.skywings.entity.Booking;
import com.skywings.entity.User;
import com.skywings.service.BookingService;
import com.skywings.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final BookingService bookingService;

    @GetMapping("/{bookingId}/eticket")
    public ResponseEntity<byte[]> downloadETicket(@PathVariable Long bookingId,
                                                   @AuthenticationPrincipal User user) {
        Booking booking = bookingService.getConfirmedBooking(bookingId, user);
        byte[] pdf = ticketService.generateETicketPdf(booking);

        String filename = "SkyWings-ETicket-" +
            (booking.getPayment() != null ? booking.getPayment().getTransactionId() : booking.getId()) +
            ".pdf";

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }
}
