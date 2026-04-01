package com.skywings.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skywings.dto.response.AmadeusFlightResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SerpApiFlightService {

    @Value("${serpapi.api-key}")
    private String apiKey;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    private boolean available = false;

    @PostConstruct
    public void init() {
        if (!"placeholder".equals(apiKey) && apiKey != null && !apiKey.isBlank()) {
            available = true;
            log.info("SerpAPI Google Flights initialized (25 free searches/month)");
        } else {
            log.warn("SerpAPI key not configured. Live flight search disabled — using DB fallback.");
        }
    }

    public boolean isAvailable() {
        return available;
    }

    public List<AmadeusFlightResponse> searchFlights(String origin, String dest,
                                                      LocalDate date, int adults) {
        return searchFlights(origin, dest, date, adults, "one_way", null);
    }

    public List<AmadeusFlightResponse> searchFlights(String origin, String dest,
                                                      LocalDate date, int adults,
                                                      String tripType, LocalDate returnDate) {
        if (!available) {
            return List.of();
        }

        String cacheKey = "serpapi:" + origin.toUpperCase() + ":" + dest.toUpperCase() + ":" + date
            + (returnDate != null ? ":" + returnDate : "");

        // Check Redis cache
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("SerpAPI cache hit for {}", cacheKey);
                return objectMapper.readValue(cached, new TypeReference<>() {});
            }
        } catch (Exception e) {
            log.debug("Cache read failed, proceeding to API call");
        }

        try {
            boolean isRoundTrip = "round_trip".equals(tripType) && returnDate != null;
            var urlBuilder = UriComponentsBuilder.fromHttpUrl("https://serpapi.com/search")
                    .queryParam("engine", "google_flights")
                    .queryParam("departure_id", origin.toUpperCase())
                    .queryParam("arrival_id", dest.toUpperCase())
                    .queryParam("outbound_date", date.toString())
                    .queryParam("type", isRoundTrip ? "1" : "2")  // 1=round trip, 2=one way
                    .queryParam("currency", "INR")
                    .queryParam("hl", "en")
                    .queryParam("gl", "in")
                    .queryParam("adults", adults)
                    .queryParam("api_key", apiKey);
            if (isRoundTrip) {
                urlBuilder.queryParam("return_date", returnDate.toString());
            }
            String url = urlBuilder.toUriString();

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            List<AmadeusFlightResponse> results = new ArrayList<>();

            // Parse best_flights
            if (root.has("best_flights")) {
                for (JsonNode flightGroup : root.get("best_flights")) {
                    AmadeusFlightResponse mapped = mapFlightGroup(flightGroup);
                    if (mapped != null) results.add(mapped);
                }
            }

            // Parse other_flights
            if (root.has("other_flights")) {
                for (JsonNode flightGroup : root.get("other_flights")) {
                    AmadeusFlightResponse mapped = mapFlightGroup(flightGroup);
                    if (mapped != null) results.add(mapped);
                }
            }

            // Cache for 10 minutes (SerpAPI has limited quota)
            try {
                redisTemplate.opsForValue().set(cacheKey,
                        objectMapper.writeValueAsString(results), 10, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.debug("Cache write failed: {}", e.getMessage());
            }

            log.info("SerpAPI returned {} flights for {} → {} on {}", results.size(), origin, dest, date);
            return results;

        } catch (Exception e) {
            log.error("SerpAPI error: {}", e.getMessage());
            return List.of();
        }
    }

    private AmadeusFlightResponse mapFlightGroup(JsonNode flightGroup) {
        try {
            JsonNode flights = flightGroup.get("flights");
            if (flights == null || flights.isEmpty()) return null;

            JsonNode firstFlight = flights.get(0);
            JsonNode lastFlight = flights.get(flights.size() - 1);

            // Departure info
            JsonNode depAirport = firstFlight.get("departure_airport");
            JsonNode arrAirport = lastFlight.get("arrival_airport");

            String airline = firstFlight.has("airline") ? firstFlight.get("airline").asText() : "Unknown";
            String flightNumber = firstFlight.has("flight_number") ? firstFlight.get("flight_number").asText() : "";
            String airlineLogo = firstFlight.has("airline_logo") ? firstFlight.get("airline_logo").asText() : null;

            // Duration
            int totalDuration = flightGroup.has("total_duration") ? flightGroup.get("total_duration").asInt() : 0;
            String durationStr = String.format("%dh %dm", totalDuration / 60, totalDuration % 60);

            // Stops
            int stops = flights.size() - 1;

            // Price
            BigDecimal price = flightGroup.has("price") ?
                    new BigDecimal(flightGroup.get("price").asText()) : BigDecimal.ZERO;

            // Carbon emissions
            String cabin = firstFlight.has("travel_class") ? firstFlight.get("travel_class").asText() : "Economy";

            return AmadeusFlightResponse.builder()
                    .amadeusOfferId(null)
                    .airline(airline)
                    .flightNumber(airline + " " + flightNumber)
                    .origin(depAirport.has("id") ? depAirport.get("id").asText() : "")
                    .destination(arrAirport.has("id") ? arrAirport.get("id").asText() : "")
                    .departureTime(depAirport.has("time") ? depAirport.get("time").asText() : "")
                    .arrivalTime(arrAirport.has("time") ? arrAirport.get("time").asText() : "")
                    .duration(durationStr)
                    .stops(stops)
                    .price(price)
                    .currency("INR")
                    .cabin(cabin)
                    .seatsAvailable(0) // Google Flights doesn't expose seat count
                    .source("GOOGLE_FLIGHTS")
                    .build();
        } catch (Exception e) {
            log.warn("Failed to parse SerpAPI flight group: {}", e.getMessage());
            return null;
        }
    }
}
