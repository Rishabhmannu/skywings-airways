package com.skywings.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FlightResponse {

    private Long id;
    private String flightNumber;
    private String airline;
    private String origin;
    private String destination;
    private String originCode;
    private String destCode;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String flightType;
    private String status;
    private BigDecimal basePriceEconomy;
    private BigDecimal basePriceBusiness;
    private Long availableEconomySeats;
    private Long availableBusinessSeats;
    private String duration;
}
