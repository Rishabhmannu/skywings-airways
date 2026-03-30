package com.skywings.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotBlank(message = "Card number is required")
    private String cardNumber;

    @NotBlank(message = "Expiry date is required")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "Expiry must be in MM/YY format")
    private String expiryDate;

    @NotBlank(message = "CVV is required")
    @Pattern(regexp = "^\\d{3,4}$", message = "CVV must be 3 or 4 digits")
    private String cvv;

    @NotBlank(message = "Payment method is required")
    @Pattern(regexp = "CREDIT_CARD|DEBIT_CARD|UPI|NET_BANKING",
             message = "Payment method must be CREDIT_CARD, DEBIT_CARD, UPI, or NET_BANKING")
    private String paymentMethod;
}
