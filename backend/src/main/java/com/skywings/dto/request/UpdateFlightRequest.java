package com.skywings.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFlightRequest {

    private String origin;
    private String originCode;
    private String destination;
    private String destCode;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String flightType;
    private String status;
    private BigDecimal basePriceEconomy;
    private BigDecimal basePriceBusiness;
}
