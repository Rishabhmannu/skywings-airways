package com.skywings.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsResponse {

    private long totalFlights;
    private long totalBookings;
    private long confirmedBookings;
    private long cancelledBookings;
    private long totalUsers;
    private BigDecimal totalRevenue;
}
