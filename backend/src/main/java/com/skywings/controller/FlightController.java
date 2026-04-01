package com.skywings.controller;

import com.skywings.dto.request.CreateFlightRequest;
import com.skywings.dto.request.UpdateFlightRequest;
import com.skywings.dto.response.AmadeusFlightResponse;
import com.skywings.dto.response.FlightResponse;
import com.skywings.dto.response.SeatMapResponse;
import com.skywings.service.SerpApiFlightService;
import com.skywings.service.FlightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;
    private final SerpApiFlightService serpApiFlightService;

    @GetMapping("/search")
    public ResponseEntity<List<FlightResponse>> searchFlights(
            @RequestParam String origin,
            @RequestParam String dest,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(flightService.searchFlights(origin, dest, date));
    }

    @GetMapping("/live-search")
    public ResponseEntity<List<AmadeusFlightResponse>> liveSearchFlights(
            @RequestParam String origin,
            @RequestParam String dest,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "1") int adults) {

        List<AmadeusFlightResponse> results = serpApiFlightService.searchFlights(
            origin, dest, date, adults);

        // Fallback to DB flights if SerpAPI is unavailable
        if (results.isEmpty()) {
            List<FlightResponse> dbFlights = flightService.searchFlights(origin, dest, date);
            results = dbFlights.stream()
                .map(f -> AmadeusFlightResponse.builder()
                    .flightNumber(f.getFlightNumber())
                    .airline("SkyWings Airways")
                    .origin(f.getOriginCode())
                    .destination(f.getDestCode())
                    .departureTime(f.getDepartureTime().toString())
                    .arrivalTime(f.getArrivalTime().toString())
                    .duration(f.getDuration())
                    .stops(0)
                    .price(f.getBasePriceEconomy())
                    .currency("INR")
                    .cabin("ECONOMY")
                    .seatsAvailable(f.getAvailableEconomySeats() != null
                        ? f.getAvailableEconomySeats().intValue() : 0)
                    .source("SKYWINGS_DB")
                    .build())
                .toList();
        }

        return ResponseEntity.ok(results);
    }

    @PostMapping("/import-live")
    public ResponseEntity<FlightResponse> importLiveFlight(@RequestBody AmadeusFlightResponse liveFlight) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(flightService.importFromLiveSearch(liveFlight));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlightResponse> getFlightById(@PathVariable Long id) {
        return ResponseEntity.ok(flightService.getFlightById(id));
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<SeatMapResponse> getSeatMap(@PathVariable Long id) {
        return ResponseEntity.ok(flightService.getSeatMap(id));
    }

    @PostMapping
    public ResponseEntity<FlightResponse> createFlight(@Valid @RequestBody CreateFlightRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(flightService.createFlight(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlightResponse> updateFlight(@PathVariable Long id,
                                                        @RequestBody UpdateFlightRequest request) {
        return ResponseEntity.ok(flightService.updateFlight(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlight(@PathVariable Long id) {
        flightService.deleteFlight(id);
        return ResponseEntity.noContent().build();
    }
}
