package com.skywings.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingResponse {

    private Long id;
    private String flightNumber;
    private String origin;
    private String destination;
    private String originCode;
    private String destCode;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String flightType;
    private String seatClass;
    private Integer numSeats;
    private String fareType;
    private BigDecimal totalPrice;
    private BigDecimal taxAmount;
    private BigDecimal penaltyAmount;
    private String status;
    private String paymentStatus;
    private String transactionId;
    private LocalDateTime bookingDate;
    private List<PassengerInfo> passengers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PassengerInfo {
        private String name;
        private Integer age;
        private String seatNumber;
        private String passportNumber;
        private String gender;
        private String dateOfBirth;
        private String nationality;
        private String mealPreference;
        private String specialAssistance;
        private Boolean isSeniorCitizen;
    }
}
