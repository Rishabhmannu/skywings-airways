package com.skywings.config;

import com.skywings.entity.Flight;
import com.skywings.entity.Seat;
import com.skywings.entity.User;
import com.skywings.entity.enums.FlightStatus;
import com.skywings.entity.enums.Role;
import com.skywings.entity.enums.SeatClass;
import com.skywings.repository.FlightRepository;
import com.skywings.repository.SeatRepository;
import com.skywings.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final FlightRepository flightRepository;
    private final SeatRepository seatRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded. Skipping.");
            return;
        }

        log.info("Seeding database with initial data...");
        seedAdmin();
        seedFlights();
        log.info("Database seeding complete.");
    }

    private void seedAdmin() {
        User admin = User.builder()
            .name("Admin")
            .email("admin@skywings.com")
            .passwordHash(passwordEncoder.encode("Admin@123"))
            .emailVerified(true)
            .phone("+911234567890")
            .role(Role.ADMIN)
            .build();
        userRepository.save(admin);
        log.info("Admin user created: admin@skywings.com / Admin@123");
    }

    private void seedFlights() {
        List<FlightData> flightDataList = List.of(
            // Domestic flights
            new FlightData("SW-101", "Delhi", "DEL", "Mumbai", "BOM",
                2, 15, "DOMESTIC", "6500", "15000"),
            new FlightData("SW-102", "Mumbai", "BOM", "Bangalore", "BLR",
                1, 45, "DOMESTIC", "5500", "13000"),
            new FlightData("SW-103", "Delhi", "DEL", "Kolkata", "CCU",
                2, 30, "DOMESTIC", "6000", "14000"),
            new FlightData("SW-104", "Chennai", "MAA", "Delhi", "DEL",
                2, 45, "DOMESTIC", "7000", "16000"),
            new FlightData("SW-105", "Bangalore", "BLR", "Hyderabad", "HYD",
                1, 15, "DOMESTIC", "4500", "11000"),
            new FlightData("SW-106", "Kolkata", "CCU", "Mumbai", "BOM",
                2, 30, "DOMESTIC", "6500", "15000"),
            new FlightData("SW-107", "Hyderabad", "HYD", "Chennai", "MAA",
                1, 10, "DOMESTIC", "4000", "10000"),
            new FlightData("SW-108", "Delhi", "DEL", "Goa", "GOI",
                2, 20, "DOMESTIC", "5500", "13000"),
            new FlightData("SW-109", "Mumbai", "BOM", "Jaipur", "JAI",
                1, 50, "DOMESTIC", "5000", "12000"),
            new FlightData("SW-110", "Bangalore", "BLR", "Delhi", "DEL",
                2, 45, "DOMESTIC", "7000", "16000"),
            // International flights
            new FlightData("SW-201", "Delhi", "DEL", "Dubai", "DXB",
                3, 30, "INTERNATIONAL", "18000", "45000"),
            new FlightData("SW-202", "Mumbai", "BOM", "Singapore", "SIN",
                5, 30, "INTERNATIONAL", "22000", "55000"),
            new FlightData("SW-203", "Delhi", "DEL", "London", "LHR",
                9, 0, "INTERNATIONAL", "35000", "85000"),
            new FlightData("SW-204", "Bangalore", "BLR", "Bangkok", "BKK",
                4, 15, "INTERNATIONAL", "15000", "38000"),
            new FlightData("SW-205", "Mumbai", "BOM", "New York", "JFK",
                16, 0, "INTERNATIONAL", "45000", "120000")
        );

        LocalDateTime baseDate = LocalDateTime.now().plusDays(7).withHour(6).withMinute(0).withSecond(0);

        for (int i = 0; i < flightDataList.size(); i++) {
            FlightData fd = flightDataList.get(i);
            LocalDateTime departure = baseDate.plusDays(i % 5).plusHours(i * 2L);
            LocalDateTime arrival = departure.plusHours(fd.durationHours).plusMinutes(fd.durationMinutes);

            Flight flight = Flight.builder()
                .flightNumber(fd.flightNumber)
                .airline("SkyWings Airways")
                .origin(fd.origin)
                .originCode(fd.originCode)
                .destination(fd.destination)
                .destCode(fd.destCode)
                .departureTime(departure)
                .arrivalTime(arrival)
                .flightType(fd.flightType)
                .status(FlightStatus.SCHEDULED)
                .basePriceEconomy(new BigDecimal(fd.economyPrice))
                .basePriceBusiness(new BigDecimal(fd.businessPrice))
                .build();

            flight = flightRepository.save(flight);
            generateSeats(flight);
        }

        log.info("Seeded {} flights with seats", flightDataList.size());
    }

    private void generateSeats(Flight flight) {
        List<Seat> seats = new ArrayList<>();
        String[] columns = {"A", "B", "C", "D", "E"};

        // Business class: rows 1-2 (10 seats)
        for (int row = 1; row <= 2; row++) {
            for (String col : columns) {
                seats.add(Seat.builder()
                    .flight(flight)
                    .seatNumber(row + col)
                    .seatClass(SeatClass.BUSINESS)
                    .isAvailable(true)
                    .price(flight.getBasePriceBusiness())
                    .build());
            }
        }

        // Economy class: rows 3-8 (30 seats)
        for (int row = 3; row <= 8; row++) {
            for (String col : columns) {
                seats.add(Seat.builder()
                    .flight(flight)
                    .seatNumber(row + col)
                    .seatClass(SeatClass.ECONOMY)
                    .isAvailable(true)
                    .price(flight.getBasePriceEconomy())
                    .build());
            }
        }

        seatRepository.saveAll(seats);
    }

    private record FlightData(String flightNumber, String origin, String originCode,
                               String destination, String destCode,
                               int durationHours, int durationMinutes,
                               String flightType, String economyPrice, String businessPrice) {}
}
