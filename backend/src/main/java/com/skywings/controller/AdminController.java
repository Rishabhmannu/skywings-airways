package com.skywings.controller;

import com.skywings.dto.response.BookingResponse;
import com.skywings.dto.response.DashboardStatsResponse;
import com.skywings.dto.response.UserProfileResponse;
import com.skywings.entity.enums.BookingStatus;
import com.skywings.repository.BookingRepository;
import com.skywings.repository.FlightRepository;
import com.skywings.repository.UserRepository;
import com.skywings.service.BookingService;
import com.skywings.service.FlightService;
import com.skywings.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final FlightService flightService;
    private final BookingService bookingService;
    private final UserService userService;
    private final FlightRepository flightRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsResponse> getDashboard() {
        long confirmedCount = bookingRepository.findAllByStatusWithDetails(BookingStatus.CONFIRMED).size();
        long cancelledCount = bookingRepository.findAllByStatusWithDetails(BookingStatus.CANCELLED).size();

        BigDecimal revenue = bookingRepository.findAllByStatusWithDetails(BookingStatus.CONFIRMED).stream()
            .map(b -> b.getTotalPrice())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        DashboardStatsResponse stats = DashboardStatsResponse.builder()
            .totalFlights(flightRepository.count())
            .totalBookings(bookingRepository.count())
            .confirmedBookings(confirmedCount)
            .cancelledBookings(cancelledCount)
            .totalUsers(userRepository.count())
            .totalRevenue(revenue)
            .build();

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> getAllBookings(
            @RequestParam(required = false) String status) {
        if (status != null) {
            return ResponseEntity.ok(bookingService.getBookingsByStatus(BookingStatus.valueOf(status)));
        }
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserProfileResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserProfileResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserProfileResponse> updateUserRole(@PathVariable Long id,
                                                               @RequestBody java.util.Map<String, String> body) {
        String role = body.get("role");
        if (role == null || (!role.equals("ADMIN") && !role.equals("PASSENGER"))) {
            throw new IllegalArgumentException("Role must be ADMIN or PASSENGER");
        }
        return ResponseEntity.ok(userService.updateRole(id, role));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<java.util.Map<String, String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(java.util.Map.of("message", "User deleted"));
    }

    @GetMapping("/flights")
    public ResponseEntity<?> getAllFlights() {
        return ResponseEntity.ok(flightService.getAllFlights());
    }
}
