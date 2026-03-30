package com.skywings.service;

import com.amadeus.Amadeus;
import com.amadeus.Configuration;
import com.amadeus.Params;
import com.amadeus.exceptions.ResponseException;
import com.amadeus.resources.FlightOfferSearch;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skywings.dto.response.AmadeusFlightResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AmadeusFlightService {

    private Amadeus amadeus;

    @Value("${amadeus.api-key}")
    private String apiKey;

    @Value("${amadeus.api-secret}")
    private String apiSecret;

    @Value("${amadeus.environment:test}")
    private String environment;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        try {
            if (!"placeholder".equals(apiKey) && apiKey != null && !apiKey.isBlank()) {
                Configuration config = Amadeus.builder(apiKey, apiSecret);
                if ("production".equals(environment)) {
                    config.setHostname("production");
                }
                amadeus = config.build();
                log.info("Amadeus API initialized (env: {})", environment);
            } else {
                log.warn("Amadeus credentials not configured. Live flight search disabled — using DB fallback.");
            }
        } catch (Exception e) {
            log.warn("Amadeus initialization failed: {}. Using DB fallback.", e.getMessage());
        }
    }

    public boolean isAvailable() {
        return amadeus != null;
    }

    public List<AmadeusFlightResponse> searchFlights(String origin, String dest,
                                                      LocalDate date, int adults) {
        if (amadeus == null) {
            return List.of();
        }

        String cacheKey = "amadeus:" + origin.toUpperCase() + ":" + dest.toUpperCase() + ":" + date;

        // Check Redis cache
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("Amadeus cache hit for {}", cacheKey);
                return objectMapper.readValue(cached, new TypeReference<>() {});
            }
        } catch (Exception e) {
            log.debug("Cache read failed, proceeding to API call");
        }

        try {
            FlightOfferSearch[] offers = amadeus.shopping.flightOffersSearch.get(
                Params.with("originLocationCode", origin.toUpperCase())
                      .and("destinationLocationCode", dest.toUpperCase())
                      .and("departureDate", date.toString())
                      .and("adults", adults)
                      .and("max", 20)
                      .and("currencyCode", "INR")
            );

            List<AmadeusFlightResponse> results = Arrays.stream(offers)
                .map(this::mapToResponse)
                .toList();

            // Cache for 5 minutes
            try {
                redisTemplate.opsForValue().set(cacheKey,
                    objectMapper.writeValueAsString(results), 5, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.debug("Cache write failed: {}", e.getMessage());
            }

            log.info("Amadeus returned {} flight offers for {} → {} on {}", results.size(), origin, dest, date);
            return results;

        } catch (ResponseException e) {
            log.error("Amadeus API error: {}", e.getMessage());
            return List.of();
        }
    }

    private AmadeusFlightResponse mapToResponse(FlightOfferSearch offer) {
        try {
            FlightOfferSearch.SearchSegment[] segments = offer.getItineraries()[0].getSegments();
            FlightOfferSearch.SearchSegment firstSegment = segments[0];
            FlightOfferSearch.SearchSegment lastSegment = segments[segments.length - 1];

            return AmadeusFlightResponse.builder()
                .amadeusOfferId(offer.getId())
                .airline(firstSegment.getCarrierCode())
                .flightNumber(firstSegment.getCarrierCode() + "-" + firstSegment.getNumber())
                .origin(firstSegment.getDeparture().getIataCode())
                .destination(lastSegment.getArrival().getIataCode())
                .departureTime(firstSegment.getDeparture().getAt())
                .arrivalTime(lastSegment.getArrival().getAt())
                .duration(offer.getItineraries()[0].getDuration())
                .stops(segments.length - 1)
                .price(new BigDecimal(offer.getPrice().getTotal()))
                .currency(offer.getPrice().getCurrency())
                .cabin(offer.getTravelerPricings() != null && offer.getTravelerPricings().length > 0
                    ? offer.getTravelerPricings()[0].getFareDetailsBySegment()[0].getCabin()
                    : "ECONOMY")
                .seatsAvailable(offer.getNumberOfBookableSeats())
                .source("AMADEUS")
                .build();
        } catch (Exception e) {
            log.warn("Failed to parse Amadeus offer {}: {}", offer.getId(), e.getMessage());
            return null;
        }
    }
}
