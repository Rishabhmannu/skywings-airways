package com.skywings.controller;

import com.skywings.dto.request.BookingRequest;
import com.skywings.dto.response.BookingResponse;
import com.skywings.entity.User;
import com.skywings.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request,
                                                          @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(bookingService.createBooking(request, user));
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getMyBookings(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bookingService.getUserBookings(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable Long id,
                                                       @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bookingService.getBooking(id, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long id,
                                                          @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bookingService.cancelBooking(id, user));
    }
}
