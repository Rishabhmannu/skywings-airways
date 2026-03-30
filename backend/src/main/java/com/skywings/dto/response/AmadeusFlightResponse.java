package com.skywings.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AmadeusFlightResponse {

    private String amadeusOfferId;
    private String airline;
    private String flightNumber;
    private String origin;
    private String destination;
    private String departureTime;
    private String arrivalTime;
    private String duration;
    private int stops;
    private BigDecimal price;
    private String currency;
    private String cabin;
    private int seatsAvailable;
    private String source;
}
