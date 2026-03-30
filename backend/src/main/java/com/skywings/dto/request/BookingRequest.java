package com.skywings.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    @NotNull(message = "Flight ID is required")
    private Long flightId;

    @NotNull(message = "Seat class is required")
    private String seatClass;

    @NotEmpty(message = "At least one passenger is required")
    @Size(max = 6, message = "Maximum 6 passengers per booking")
    @Valid
    private List<PassengerDetail> passengers;
}
