package com.skywings.service;

import com.skywings.entity.Flight;
import com.skywings.entity.enums.SeatClass;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

@Service
public class PricingService {

    private static final BigDecimal DOMESTIC_TAX_RATE = new BigDecimal("0.05");
    private static final BigDecimal INTERNATIONAL_TAX_RATE = new BigDecimal("0.08");
    private static final BigDecimal CANCELLATION_PENALTY_RATE = new BigDecimal("0.25");

    public PricingResult calculate(Flight flight, SeatClass seatClass, int numSeats) {
        BigDecimal basePrice = switch (seatClass) {
            case ECONOMY -> flight.getBasePriceEconomy();
            case BUSINESS -> flight.getBasePriceBusiness();
        };

        long durationMinutes = Duration.between(flight.getDepartureTime(), flight.getArrivalTime()).toMinutes();
        BigDecimal durationHours = new BigDecimal(durationMinutes)
            .divide(new BigDecimal("60"), 2, RoundingMode.HALF_UP);

        // Minimum 1 hour for pricing
        if (durationHours.compareTo(BigDecimal.ONE) < 0) {
            durationHours = BigDecimal.ONE;
        }

        BigDecimal subtotal = basePrice.multiply(durationHours)
            .multiply(BigDecimal.valueOf(numSeats))
            .setScale(2, RoundingMode.HALF_UP);

        BigDecimal taxRate = flight.getFlightType().equals("INTERNATIONAL")
            ? INTERNATIONAL_TAX_RATE : DOMESTIC_TAX_RATE;

        BigDecimal tax = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tax);

        return new PricingResult(subtotal, tax, total);
    }

    public BigDecimal calculatePenalty(BigDecimal totalPrice) {
        return totalPrice.multiply(CANCELLATION_PENALTY_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    public record PricingResult(BigDecimal subtotal, BigDecimal tax, BigDecimal total) {}
}
