package com.skywings.service;

import com.skywings.dto.request.BookingRequest;
import com.skywings.dto.request.PassengerDetail;
import com.skywings.dto.response.BookingResponse;
import com.skywings.entity.*;
import com.skywings.entity.enums.BookingStatus;
import com.skywings.entity.enums.SeatClass;
import com.skywings.exception.BookingNotCancellableException;
import com.skywings.exception.InsufficientSeatsException;
import com.skywings.exception.ResourceNotFoundException;
import com.skywings.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final SeatRepository seatRepository;
    private final BookingPassengerRepository bookingPassengerRepository;
    private final PricingService pricingService;
    private final CacheManager cacheManager;

    @Transactional
    public BookingResponse createBooking(BookingRequest request, User user) {
        Flight flight = flightRepository.findById(request.getFlightId())
            .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));

        if (flight.getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new BookingNotCancellableException("Cannot book a flight that has already departed");
        }

        SeatClass seatClass = SeatClass.valueOf(request.getSeatClass().toUpperCase());

        // Pessimistic lock on seats
        List<Seat> availableSeats = seatRepository.findAvailableSeatsWithLock(
            flight.getId(), seatClass);

        if (availableSeats.size() < request.getPassengers().size()) {
            throw new InsufficientSeatsException(
                "Only " + availableSeats.size() + " " + seatClass + " seats available");
        }

        // Validate passport for international flights
        if ("INTERNATIONAL".equals(flight.getFlightType())) {
            for (PassengerDetail pd : request.getPassengers()) {
                if (pd.getPassportNumber() == null || pd.getPassportNumber().isBlank()) {
                    throw new IllegalArgumentException(
                        "Passport number is required for international flights for passenger: " + pd.getName());
                }
            }
        }

        // Assign seats
        List<Seat> assignedSeats = availableSeats.subList(0, request.getPassengers().size());
        assignedSeats.forEach(seat -> seat.setIsAvailable(false));
        seatRepository.saveAll(assignedSeats);

        // Calculate pricing with fare type
        String fareType = request.getFareType() != null ? request.getFareType() : "REGULAR";
        PricingService.PricingResult pricing = pricingService.calculate(
            flight, seatClass, request.getPassengers().size(), fareType);

        // Create booking
        Booking booking = Booking.builder()
            .user(user)
            .flight(flight)
            .bookingDate(LocalDateTime.now())
            .status(BookingStatus.PENDING)
            .seatClass(seatClass)
            .numSeats(request.getPassengers().size())
            .fareType(fareType)
            .totalPrice(pricing.total())
            .taxAmount(pricing.tax())
            .penaltyAmount(BigDecimal.ZERO)
            .build();
        booking = bookingRepository.save(booking);

        // Create passenger entries
        for (int i = 0; i < request.getPassengers().size(); i++) {
            PassengerDetail pd = request.getPassengers().get(i);
            boolean isSenior = pd.getAge() >= 60;
            BookingPassenger bp = BookingPassenger.builder()
                .booking(booking)
                .seat(assignedSeats.get(i))
                .passengerName(pd.getName())
                .age(pd.getAge())
                .passportNumber(pd.getPassportNumber())
                .gender(pd.getGender())
                .dateOfBirth(pd.getDateOfBirth())
                .nationality(pd.getNationality())
                .mealPreference(pd.getMealPreference())
                .specialAssistance(pd.getSpecialAssistance())
                .isSeniorCitizen(isSenior)
                .build();
            bookingPassengerRepository.save(bp);
        }

        evictFlightCache();
        log.info("Booking created: {} for user: {}", booking.getId(), user.getEmail());
        return toBookingResponse(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId, User user) {
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BookingNotCancellableException(
                "Only confirmed bookings can be cancelled. Current status: " + booking.getStatus());
        }

        if (booking.getFlight().getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new BookingNotCancellableException("Cannot cancel a booking for a departed flight");
        }

        BigDecimal penalty = pricingService.calculatePenalty(booking.getTotalPrice());
        booking.setPenaltyAmount(penalty);
        booking.setStatus(BookingStatus.CANCELLED);

        // Release seats
        List<BookingPassenger> passengers = bookingPassengerRepository.findByBookingId(bookingId);
        for (BookingPassenger bp : passengers) {
            if (bp.getSeat() != null) {
                bp.getSeat().setIsAvailable(true);
                seatRepository.save(bp.getSeat());
            }
        }

        // Update payment status if exists
        if (booking.getPayment() != null) {
            booking.getPayment().setStatus(com.skywings.entity.enums.PaymentStatus.REFUNDED);
        }

        bookingRepository.save(booking);
        evictFlightCache();
        log.info("Booking cancelled: {} with penalty: {}", bookingId, penalty);
        return toBookingResponse(booking);
    }

    public List<BookingResponse> getUserBookings(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(this::toBookingResponse)
            .toList();
    }

    public BookingResponse getBooking(Long bookingId, User user) {
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        return toBookingResponse(booking);
    }

    public Booking getConfirmedBooking(Long bookingId, User user) {
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BookingNotCancellableException("Booking is not confirmed");
        }
        return booking;
    }

    public Booking getBookingEntity(Long bookingId) {
        return bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    // Admin methods
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAllWithDetails().stream()
            .map(this::toBookingResponse)
            .toList();
    }

    public List<BookingResponse> getBookingsByStatus(BookingStatus status) {
        return bookingRepository.findAllByStatusWithDetails(status).stream()
            .map(this::toBookingResponse)
            .toList();
    }

    @Scheduled(fixedRate = 60000) // Every 1 minute
    @Transactional
    public void expireUnpaidBookings() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(15);
        List<Booking> expired = bookingRepository.findByStatusAndCreatedAtBefore(
            BookingStatus.PENDING, cutoff);

        for (Booking booking : expired) {
            booking.setStatus(BookingStatus.CANCELLED);
            List<BookingPassenger> passengers = bookingPassengerRepository.findByBookingId(booking.getId());
            for (BookingPassenger bp : passengers) {
                if (bp.getSeat() != null) {
                    bp.getSeat().setIsAvailable(true);
                    seatRepository.save(bp.getSeat());
                }
            }
            bookingRepository.save(booking);
            log.info("Expired unpaid booking: {}", booking.getId());
        }

        if (!expired.isEmpty()) {
            evictFlightCache();
        }
    }

    private BookingResponse toBookingResponse(Booking booking) {
        Flight flight = booking.getFlight();
        List<BookingPassenger> passengers = bookingPassengerRepository.findByBookingId(booking.getId());

        List<BookingResponse.PassengerInfo> passengerInfos = passengers.stream()
            .map(bp -> BookingResponse.PassengerInfo.builder()
                .name(bp.getPassengerName())
                .age(bp.getAge())
                .seatNumber(bp.getSeat() != null ? bp.getSeat().getSeatNumber() : null)
                .passportNumber(bp.getPassportNumber())
                .gender(bp.getGender())
                .dateOfBirth(bp.getDateOfBirth())
                .nationality(bp.getNationality())
                .mealPreference(bp.getMealPreference())
                .specialAssistance(bp.getSpecialAssistance())
                .isSeniorCitizen(bp.getIsSeniorCitizen())
                .build())
            .toList();

        return BookingResponse.builder()
            .id(booking.getId())
            .flightNumber(flight.getFlightNumber())
            .origin(flight.getOrigin())
            .destination(flight.getDestination())
            .originCode(flight.getOriginCode())
            .destCode(flight.getDestCode())
            .departureTime(flight.getDepartureTime())
            .arrivalTime(flight.getArrivalTime())
            .flightType(flight.getFlightType())
            .seatClass(booking.getSeatClass().name())
            .numSeats(booking.getNumSeats())
            .fareType(booking.getFareType())
            .totalPrice(booking.getTotalPrice())
            .taxAmount(booking.getTaxAmount())
            .penaltyAmount(booking.getPenaltyAmount())
            .status(booking.getStatus().name())
            .paymentStatus(booking.getPayment() != null ? booking.getPayment().getStatus().name() : null)
            .transactionId(booking.getPayment() != null ? booking.getPayment().getTransactionId() : null)
            .bookingDate(booking.getBookingDate())
            .passengers(passengerInfos)
            .build();
    }

    private void evictFlightCache() {
        var cache = cacheManager.getCache("flights");
        if (cache != null) cache.clear();
    }
}
