package com.skywings.controller;

import com.skywings.dto.response.FlightResponse;
import com.skywings.exception.GlobalExceptionHandler;
import com.skywings.exception.ResourceNotFoundException;
import com.skywings.service.SerpApiFlightService;
import com.skywings.service.FlightService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FlightController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class FlightControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private FlightService flightService;
    @MockBean private SerpApiFlightService serpApiFlightService;
    @MockBean private com.skywings.service.JwtService jwtService;
    @MockBean private com.skywings.config.JwtAuthenticationFilter jwtAuthFilter;
    @MockBean private com.skywings.repository.UserRepository userRepository;

    @Test
    void searchFlights_returnsResults() throws Exception {
        FlightResponse flight = FlightResponse.builder()
                .id(1L).flightNumber("SW-101").airline("SkyWings Airways")
                .origin("Delhi").destination("Mumbai").originCode("DEL").destCode("BOM")
                .departureTime(LocalDateTime.of(2026, 4, 15, 8, 0))
                .arrivalTime(LocalDateTime.of(2026, 4, 15, 10, 15))
                .flightType("DOMESTIC").status("SCHEDULED")
                .basePriceEconomy(new BigDecimal("6500"))
                .basePriceBusiness(new BigDecimal("15000"))
                .availableEconomySeats(30L).availableBusinessSeats(10L)
                .duration("2h 15m").build();

        when(flightService.searchFlights("DEL", "BOM", LocalDate.of(2026, 4, 15)))
                .thenReturn(List.of(flight));

        mockMvc.perform(get("/api/flights/search")
                        .param("origin", "DEL")
                        .param("dest", "BOM")
                        .param("date", "2026-04-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].flightNumber").value("SW-101"))
                .andExpect(jsonPath("$[0].originCode").value("DEL"))
                .andExpect(jsonPath("$[0].availableEconomySeats").value(30));
    }

    @Test
    void searchFlights_noResults_returnsEmptyList() throws Exception {
        when(flightService.searchFlights(anyString(), anyString(), any(LocalDate.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/flights/search")
                        .param("origin", "XXX")
                        .param("dest", "YYY")
                        .param("date", "2026-04-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getFlightById_found_returnsDetails() throws Exception {
        FlightResponse flight = FlightResponse.builder()
                .id(1L).flightNumber("SW-101").airline("SkyWings Airways")
                .originCode("DEL").destCode("BOM").build();

        when(flightService.getFlightById(1L)).thenReturn(flight);

        mockMvc.perform(get("/api/flights/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value("SW-101"));
    }

    @Test
    void getFlightById_notFound_returns404() throws Exception {
        when(flightService.getFlightById(999L))
                .thenThrow(new ResourceNotFoundException("Flight not found with id: 999"));

        mockMvc.perform(get("/api/flights/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Flight not found with id: 999"));
    }
}
