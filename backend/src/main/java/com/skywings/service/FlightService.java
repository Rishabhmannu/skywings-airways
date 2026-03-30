package com.skywings.service;

import com.skywings.dto.request.CreateFlightRequest;
import com.skywings.dto.request.UpdateFlightRequest;
import com.skywings.dto.response.FlightResponse;
import com.skywings.dto.response.SeatMapResponse;
import com.skywings.entity.Flight;
import com.skywings.entity.Seat;
import com.skywings.entity.enums.FlightStatus;
import com.skywings.entity.enums.SeatClass;
import com.skywings.exception.DuplicateResourceException;
import com.skywings.exception.ResourceNotFoundException;
import com.skywings.repository.FlightRepository;
import com.skywings.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlightService {

    private final FlightRepository flightRepository;
    private final SeatRepository seatRepository;

    @Cacheable(value = "flights", key = "#origin + ':' + #dest + ':' + #date")
    public List<FlightResponse> searchFlights(String origin, String dest, LocalDate date) {
        List<Flight> flights = flightRepository.searchFlights(
            origin.toUpperCase(), dest.toUpperCase(), date);

        return flights.stream().map(this::toFlightResponse).toList();
    }

    public FlightResponse getFlightById(Long id) {
        Flight flight = flightRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Flight not found with id: " + id));
        return toFlightResponse(flight);
    }

    public SeatMapResponse getSeatMap(Long flightId) {
        Flight flight = flightRepository.findById(flightId)
            .orElseThrow(() -> new ResourceNotFoundException("Flight not found with id: " + flightId));

        List<Seat> seats = seatRepository.findAllByFlightId(flightId);
        List<SeatMapResponse.SeatInfo> seatInfos = seats.stream()
            .map(s -> SeatMapResponse.SeatInfo.builder()
                .id(s.getId())
                .seatNumber(s.getSeatNumber())
                .seatClass(s.getSeatClass().name())
                .available(s.getIsAvailable())
                .price(s.getPrice())
                .build())
            .toList();

        return SeatMapResponse.builder()
            .flightId(flight.getId())
            .flightNumber(flight.getFlightNumber())
            .seats(seatInfos)
            .build();
    }

    @Transactional
    @CacheEvict(value = "flights", allEntries = true)
    public FlightResponse createFlight(CreateFlightRequest request) {
        if (flightRepository.existsByFlightNumber(request.getFlightNumber())) {
            throw new DuplicateResourceException("Flight number already exists: " + request.getFlightNumber());
        }

        Flight flight = Flight.builder()
            .flightNumber(request.getFlightNumber())
            .airline("SkyWings Airways")
            .origin(request.getOrigin())
            .originCode(request.getOriginCode().toUpperCase())
            .destination(request.getDestination())
            .destCode(request.getDestCode().toUpperCase())
            .departureTime(request.getDepartureTime())
            .arrivalTime(request.getArrivalTime())
            .flightType(request.getFlightType())
            .status(FlightStatus.SCHEDULED)
            .basePriceEconomy(request.getBasePriceEconomy())
            .basePriceBusiness(request.getBasePriceBusiness())
            .build();

        flight = flightRepository.save(flight);
        generateSeats(flight);
        return toFlightResponse(flight);
    }

    @Transactional
    @CacheEvict(value = "flights", allEntries = true)
    public FlightResponse updateFlight(Long id, UpdateFlightRequest request) {
        Flight flight = flightRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Flight not found with id: " + id));

        if (request.getOrigin() != null) flight.setOrigin(request.getOrigin());
        if (request.getOriginCode() != null) flight.setOriginCode(request.getOriginCode().toUpperCase());
        if (request.getDestination() != null) flight.setDestination(request.getDestination());
        if (request.getDestCode() != null) flight.setDestCode(request.getDestCode().toUpperCase());
        if (request.getDepartureTime() != null) flight.setDepartureTime(request.getDepartureTime());
        if (request.getArrivalTime() != null) flight.setArrivalTime(request.getArrivalTime());
        if (request.getFlightType() != null) flight.setFlightType(request.getFlightType());
        if (request.getStatus() != null) flight.setStatus(FlightStatus.valueOf(request.getStatus()));
        if (request.getBasePriceEconomy() != null) flight.setBasePriceEconomy(request.getBasePriceEconomy());
        if (request.getBasePriceBusiness() != null) flight.setBasePriceBusiness(request.getBasePriceBusiness());

        flight = flightRepository.save(flight);
        return toFlightResponse(flight);
    }

    @Transactional
    @CacheEvict(value = "flights", allEntries = true)
    public void deleteFlight(Long id) {
        if (!flightRepository.existsById(id)) {
            throw new ResourceNotFoundException("Flight not found with id: " + id);
        }
        flightRepository.deleteById(id);
    }

    public List<FlightResponse> getAllFlights() {
        return flightRepository.findUpcomingFlights().stream()
            .map(this::toFlightResponse)
            .toList();
    }

    private FlightResponse toFlightResponse(Flight flight) {
        long economySeats = seatRepository.countAvailableSeats(flight.getId(), SeatClass.ECONOMY);
        long businessSeats = seatRepository.countAvailableSeats(flight.getId(), SeatClass.BUSINESS);

        Duration duration = Duration.between(flight.getDepartureTime(), flight.getArrivalTime());
        String durationStr = String.format("%dh %dm", duration.toHours(), duration.toMinutesPart());

        return FlightResponse.builder()
            .id(flight.getId())
            .flightNumber(flight.getFlightNumber())
            .airline(flight.getAirline())
            .origin(flight.getOrigin())
            .destination(flight.getDestination())
            .originCode(flight.getOriginCode())
            .destCode(flight.getDestCode())
            .departureTime(flight.getDepartureTime())
            .arrivalTime(flight.getArrivalTime())
            .flightType(flight.getFlightType())
            .status(flight.getStatus().name())
            .basePriceEconomy(flight.getBasePriceEconomy())
            .basePriceBusiness(flight.getBasePriceBusiness())
            .availableEconomySeats(economySeats)
            .availableBusinessSeats(businessSeats)
            .duration(durationStr)
            .build();
    }

    private void generateSeats(Flight flight) {
        List<Seat> seats = new ArrayList<>();
        String[] columns = {"A", "B", "C", "D", "E"};

        for (int row = 1; row <= 2; row++) {
            for (String col : columns) {
                seats.add(Seat.builder()
                    .flight(flight).seatNumber(row + col)
                    .seatClass(SeatClass.BUSINESS).isAvailable(true)
                    .price(flight.getBasePriceBusiness()).build());
            }
        }
        for (int row = 3; row <= 8; row++) {
            for (String col : columns) {
                seats.add(Seat.builder()
                    .flight(flight).seatNumber(row + col)
                    .seatClass(SeatClass.ECONOMY).isAvailable(true)
                    .price(flight.getBasePriceEconomy()).build());
            }
        }
        seatRepository.saveAll(seats);
    }
}
