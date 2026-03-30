package com.skywings.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponse {

    private String transactionId;
    private BigDecimal amount;
    private String paymentMethod;
    private String cardLastFour;
    private String status;
    private List<String> otpSentVia;
    private String message;
    private Integer remainingAttempts;
}
