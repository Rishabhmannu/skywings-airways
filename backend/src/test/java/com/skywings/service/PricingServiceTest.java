package com.skywings.service;

import com.skywings.entity.Flight;
import com.skywings.entity.enums.SeatClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PricingServiceTest {

    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        pricingService = new PricingService();
    }

    private Flight buildFlight(String type, String economyPrice, String businessPrice, int durationHours) {
        LocalDateTime dep = LocalDateTime.of(2026, 4, 15, 8, 0);
        return Flight.builder()
                .flightType(type)
                .basePriceEconomy(new BigDecimal(economyPrice))
                .basePriceBusiness(new BigDecimal(businessPrice))
                .departureTime(dep)
                .arrivalTime(dep.plusHours(durationHours))
                .build();
    }

    @Test
    void calculate_domesticEconomy_appliesFivePercentTax() {
        Flight flight = buildFlight("DOMESTIC", "6500", "15000", 2);

        PricingService.PricingResult result = pricingService.calculate(flight, SeatClass.ECONOMY, 1);

        // 6500 * 2 hours * 1 pax = 13000 subtotal
        // 5% tax = 650
        // total = 13650
        assertThat(result.subtotal()).isEqualByComparingTo("13000.00");
        assertThat(result.tax()).isEqualByComparingTo("650.00");
        assertThat(result.total()).isEqualByComparingTo("13650.00");
    }

    @Test
    void calculate_internationalBusiness_appliesEightPercentTax() {
        Flight flight = buildFlight("INTERNATIONAL", "18000", "45000", 3);

        PricingService.PricingResult result = pricingService.calculate(flight, SeatClass.BUSINESS, 2);

        // 45000 * 3 hours * 2 pax = 270000 subtotal
        // 8% tax = 21600
        // total = 291600
        assertThat(result.subtotal()).isEqualByComparingTo("270000.00");
        assertThat(result.tax()).isEqualByComparingTo("21600.00");
        assertThat(result.total()).isEqualByComparingTo("291600.00");
    }

    @Test
    void calculate_shortFlight_usesMinimumOneHour() {
        LocalDateTime dep = LocalDateTime.of(2026, 4, 15, 8, 0);
        Flight flight = Flight.builder()
                .flightType("DOMESTIC")
                .basePriceEconomy(new BigDecimal("5000"))
                .basePriceBusiness(new BigDecimal("12000"))
                .departureTime(dep)
                .arrivalTime(dep.plusMinutes(45)) // Less than 1 hour
                .build();

        PricingService.PricingResult result = pricingService.calculate(flight, SeatClass.ECONOMY, 1);

        // Should use minimum 1 hour: 5000 * 1 * 1 = 5000
        assertThat(result.subtotal()).isEqualByComparingTo("5000.00");
    }

    @Test
    void calculate_multiplePassengers_multipliesCorrectly() {
        Flight flight = buildFlight("DOMESTIC", "6500", "15000", 2);

        PricingService.PricingResult result = pricingService.calculate(flight, SeatClass.ECONOMY, 4);

        // 6500 * 2 * 4 = 52000
        assertThat(result.subtotal()).isEqualByComparingTo("52000.00");
    }

    @Test
    void calculatePenalty_returnsTwentyFivePercent() {
        BigDecimal total = new BigDecimal("13650.00");

        BigDecimal penalty = pricingService.calculatePenalty(total);

        assertThat(penalty).isEqualByComparingTo("3412.50");
    }
}
