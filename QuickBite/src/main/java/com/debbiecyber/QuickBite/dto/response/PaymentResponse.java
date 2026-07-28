package com.debbiecyber.QuickBite.dto.response;


import com.debbiecyber.QuickBite.enums.PaymentMethod;
import com.debbiecyber.QuickBite.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private Long id;

    private Long orderId;

    private String reference;

    private Double amount;

    private PaymentStatus paymentStatus;

    private PaymentMethod paymentMethod;

    private String paymentURL;

    private LocalDateTime paidAt;

    private LocalDateTime createdAt;
}
