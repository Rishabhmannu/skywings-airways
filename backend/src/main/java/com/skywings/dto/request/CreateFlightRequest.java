package com.skywings.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateFlightRequest {

    @NotBlank(message = "Flight number is required")
    @Size(max = 10)
    private String flightNumber;

    @NotBlank(message = "Origin city is required")
    private String origin;

    @NotBlank(message = "Origin code is required")
    @Size(min = 3, max = 5)
    private String originCode;

    @NotBlank(message = "Destination city is required")
    private String destination;

    @NotBlank(message = "Destination code is required")
    @Size(min = 3, max = 5)
    private String destCode;

    @NotNull(message = "Departure time is required")
    @Future(message = "Departure time must be in the future")
    private LocalDateTime departureTime;

    @NotNull(message = "Arrival time is required")
    @Future(message = "Arrival time must be in the future")
    private LocalDateTime arrivalTime;

    @NotBlank(message = "Flight type is required")
    @Pattern(regexp = "DOMESTIC|INTERNATIONAL", message = "Flight type must be DOMESTIC or INTERNATIONAL")
    private String flightType;

    @NotNull(message = "Economy price is required")
    @DecimalMin(value = "0.01", message = "Economy price must be positive")
    private BigDecimal basePriceEconomy;

    @NotNull(message = "Business price is required")
    @DecimalMin(value = "0.01", message = "Business price must be positive")
    private BigDecimal basePriceBusiness;
}
