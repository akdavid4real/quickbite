package com.debbiecyber.QuickBite.dto.response;


import com.debbiecyber.QuickBite.enums.OrderStatus;
import com.debbiecyber.QuickBite.enums.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long id;

    private Long customerId;

    private String customerName;

    private Long restaurantId;

    private String restaurantName;

    private Long riderId;

    private String riderName;

    private OrderStatus orderStatus;

    private List<OrderItemResponse> orderItems;

    private PaymentMethod paymentMethod;

    private String deliveryAddress;

    private Double subTotal;

    private Double deliveryFee;

    private Double totalAmount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
