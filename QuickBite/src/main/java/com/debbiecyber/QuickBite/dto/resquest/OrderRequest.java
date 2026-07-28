package com.debbiecyber.QuickBite.dto.resquest;

import com.debbiecyber.QuickBite.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRequest {

    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}
