package com.skywings.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatMapResponse {

    private Long flightId;
    private String flightNumber;
    private List<SeatInfo> seats;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SeatInfo {
        private Long id;
        private String seatNumber;
        private String seatClass;
        private boolean available;
        private BigDecimal price;
    }
}
