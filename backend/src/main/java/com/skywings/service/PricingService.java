package com.skywings.service;

import com.skywings.entity.Flight;
import com.skywings.entity.enums.SeatClass;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Map;

@Service
public class PricingService {

    private static final BigDecimal DOMESTIC_TAX_RATE = new BigDecimal("0.05");
    private static final BigDecimal INTERNATIONAL_TAX_RATE = new BigDecimal("0.08");
    private static final BigDecimal CANCELLATION_PENALTY_RATE = new BigDecimal("0.25");

    private static final Map<String, BigDecimal> FARE_DISCOUNTS = Map.of(
        "REGULAR", BigDecimal.ZERO,
        "STUDENT", new BigDecimal("0.10"),
        "ARMED_FORCES", new BigDecimal("0.15"),
        "SENIOR_CITIZEN", new BigDecimal("0.12"),
        "MEDICAL", new BigDecimal("0.10")
    );

    public PricingResult calculate(Flight flight, SeatClass seatClass, int numSeats, String fareType) {
        BigDecimal basePrice = switch (seatClass) {
            case ECONOMY -> flight.getBasePriceEconomy();
            case BUSINESS -> flight.getBasePriceBusiness();
        };

        long durationMinutes = Duration.between(flight.getDepartureTime(), flight.getArrivalTime()).toMinutes();
        BigDecimal durationHours = new BigDecimal(durationMinutes)
            .divide(new BigDecimal("60"), 2, RoundingMode.HALF_UP);

        if (durationHours.compareTo(BigDecimal.ONE) < 0) {
            durationHours = BigDecimal.ONE;
        }

        BigDecimal subtotal = basePrice.multiply(durationHours)
            .multiply(BigDecimal.valueOf(numSeats))
            .setScale(2, RoundingMode.HALF_UP);

        // Apply fare discount
        BigDecimal discountRate = FARE_DISCOUNTS.getOrDefault(
            fareType != null ? fareType : "REGULAR", BigDecimal.ZERO);
        BigDecimal discount = subtotal.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
        subtotal = subtotal.subtract(discount);

        BigDecimal taxRate = flight.getFlightType().equals("INTERNATIONAL")
            ? INTERNATIONAL_TAX_RATE : DOMESTIC_TAX_RATE;

        BigDecimal tax = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tax);

        return new PricingResult(subtotal, tax, total, discount, fareType != null ? fareType : "REGULAR");
    }

    // Backward-compatible overload
    public PricingResult calculate(Flight flight, SeatClass seatClass, int numSeats) {
        return calculate(flight, seatClass, numSeats, "REGULAR");
    }

    public BigDecimal calculatePenalty(BigDecimal totalPrice) {
        return totalPrice.multiply(CANCELLATION_PENALTY_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal getDiscountRate(String fareType) {
        return FARE_DISCOUNTS.getOrDefault(fareType, BigDecimal.ZERO);
    }

    public record PricingResult(BigDecimal subtotal, BigDecimal tax, BigDecimal total,
                                 BigDecimal discount, String fareType) {}
}
