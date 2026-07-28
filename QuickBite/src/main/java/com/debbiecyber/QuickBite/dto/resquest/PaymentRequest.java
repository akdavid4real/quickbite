package com.debbiecyber.QuickBite.dto.resquest;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotNull(message = "Order id is required")
    private Long orderId;
}
