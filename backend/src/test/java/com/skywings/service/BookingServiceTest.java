package com.skywings.service;

import com.skywings.dto.request.BookingRequest;
import com.skywings.dto.request.PassengerDetail;
import com.skywings.dto.response.BookingResponse;
import com.skywings.entity.*;
import com.skywings.entity.enums.*;
import com.skywings.exception.BookingNotCancellableException;
import com.skywings.exception.InsufficientSeatsException;
import com.skywings.exception.ResourceNotFoundException;
import com.skywings.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private FlightRepository flightRepository;
    @Mock private SeatRepository seatRepository;
    @Mock private BookingPassengerRepository bookingPassengerRepository;
    @Mock private PricingService pricingService;
    @Mock private CacheManager cacheManager;

    @InjectMocks private BookingService bookingService;

    private User testUser() {
        return User.builder().id(1L).name("Rishabh").email("r@test.com")
                .phone("+911234567890").role(Role.PASSENGER).build();
    }

    private Flight testFlight() {
        return Flight.builder().id(1L).flightNumber("SW-101")
                .origin("Delhi").originCode("DEL")
                .destination("Mumbai").destCode("BOM")
                .departureTime(LocalDateTime.now().plusDays(7))
                .arrivalTime(LocalDateTime.now().plusDays(7).plusHours(2))
                .flightType("DOMESTIC")
                .basePriceEconomy(new BigDecimal("6500"))
                .basePriceBusiness(new BigDecimal("15000"))
                .status(FlightStatus.SCHEDULED)
                .build();
    }

    private PassengerDetail testPassenger(String name, int age, String passport) {
        PassengerDetail pd = new PassengerDetail();
        pd.setName(name);
        pd.setAge(age);
        pd.setPassportNumber(passport);
        return pd;
    }

    @Test
    void createBooking_withAvailableSeats_shouldSucceed() {
        Flight flight = testFlight();
        User user = testUser();
        List<Seat> seats = List.of(
                Seat.builder().id(1L).seatNumber("3A").seatClass(SeatClass.ECONOMY).isAvailable(true).price(new BigDecimal("6500")).build(),
                Seat.builder().id(2L).seatNumber("3B").seatClass(SeatClass.ECONOMY).isAvailable(true).price(new BigDecimal("6500")).build()
        );
        BookingRequest request = new BookingRequest();
        request.setFlightId(1L);
        request.setSeatClass("ECONOMY");
        request.setPassengers(List.of(testPassenger("Rishabh", 22, null)));

        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(seatRepository.findAvailableSeatsWithLock(1L, SeatClass.ECONOMY)).thenReturn(seats);
        when(pricingService.calculate(any(), eq(SeatClass.ECONOMY), eq(1), any()))
                .thenReturn(new PricingService.PricingResult(
                        new BigDecimal("13000"), new BigDecimal("650"), new BigDecimal("13650"),
                        BigDecimal.ZERO, "REGULAR"));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(1L);
            return b;
        });
        when(bookingPassengerRepository.findByBookingId(1L)).thenReturn(List.of());
        when(cacheManager.getCache("flights")).thenReturn(null);

        BookingResponse result = bookingService.createBooking(request, user);

        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getTotalPrice()).isEqualByComparingTo("13650");
        assertThat(result.getSeatClass()).isEqualTo("ECONOMY");
        verify(seatRepository).saveAll(anyList());
    }

    @Test
    void createBooking_withInsufficientSeats_shouldThrow() {
        Flight flight = testFlight();
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(seatRepository.findAvailableSeatsWithLock(1L, SeatClass.ECONOMY)).thenReturn(List.of());

        BookingRequest request = new BookingRequest();
        request.setFlightId(1L);
        request.setSeatClass("ECONOMY");
        request.setPassengers(List.of(testPassenger("Rishabh", 22, null)));

        assertThatThrownBy(() -> bookingService.createBooking(request, testUser()))
                .isInstanceOf(InsufficientSeatsException.class)
                .hasMessageContaining("0 ECONOMY seats available");
    }

    @Test
    void createBooking_flightNotFound_shouldThrow() {
        when(flightRepository.findById(999L)).thenReturn(Optional.empty());

        BookingRequest request = new BookingRequest();
        request.setFlightId(999L);
        request.setSeatClass("ECONOMY");
        request.setPassengers(List.of(testPassenger("Test", 25, null)));

        assertThatThrownBy(() -> bookingService.createBooking(request, testUser()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createBooking_internationalWithoutPassport_shouldThrow() {
        Flight flight = testFlight();
        flight.setFlightType("INTERNATIONAL");
        List<Seat> seats = List.of(
                Seat.builder().id(1L).seatNumber("3A").seatClass(SeatClass.ECONOMY).isAvailable(true).price(new BigDecimal("18000")).build()
        );

        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(seatRepository.findAvailableSeatsWithLock(1L, SeatClass.ECONOMY)).thenReturn(seats);

        BookingRequest request = new BookingRequest();
        request.setFlightId(1L);
        request.setSeatClass("ECONOMY");
        request.setPassengers(List.of(testPassenger("Rishabh", 22, null)));

        assertThatThrownBy(() -> bookingService.createBooking(request, testUser()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Passport number is required");
    }

    @Test
    void cancelBooking_confirmed_shouldApplyPenalty() {
        User user = testUser();
        Flight flight = testFlight();
        Seat seat = Seat.builder().id(1L).seatNumber("3A").isAvailable(false).build();
        BookingPassenger bp = BookingPassenger.builder().id(1L).seat(seat).passengerName("Rishabh").age(22).build();
        Payment payment = Payment.builder().id(1L).status(PaymentStatus.COMPLETED).transactionId("SKY-1234-5678").build();
        Booking booking = Booking.builder().id(1L).user(user).flight(flight)
                .status(BookingStatus.CONFIRMED).seatClass(SeatClass.ECONOMY)
                .numSeats(1).totalPrice(new BigDecimal("13650"))
                .taxAmount(new BigDecimal("650")).penaltyAmount(BigDecimal.ZERO)
                .bookingDate(LocalDateTime.now()).payment(payment).build();

        when(bookingRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(booking));
        when(bookingPassengerRepository.findByBookingId(1L)).thenReturn(List.of(bp));
        when(pricingService.calculatePenalty(new BigDecimal("13650"))).thenReturn(new BigDecimal("3412.50"));
        when(bookingRepository.save(any())).thenReturn(booking);
        when(cacheManager.getCache("flights")).thenReturn(null);

        BookingResponse result = bookingService.cancelBooking(1L, user);

        assertThat(result.getStatus()).isEqualTo("CANCELLED");
        verify(seatRepository).save(seat);
        assertThat(seat.getIsAvailable()).isTrue();
    }

    @Test
    void cancelBooking_pendingStatus_shouldThrow() {
        User user = testUser();
        Booking booking = Booking.builder().id(1L).user(user).flight(testFlight())
                .status(BookingStatus.PENDING).build();

        when(bookingRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(1L, user))
                .isInstanceOf(BookingNotCancellableException.class)
                .hasMessageContaining("Only confirmed bookings");
    }
}
