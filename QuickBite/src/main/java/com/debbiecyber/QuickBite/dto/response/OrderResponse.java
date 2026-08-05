package com.debbiecyber.QuickBite.dto.response;


import com.debbiecyber.QuickBite.enums.OrderStatus;
import com.debbiecyber.QuickBite.enums.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

@Data
@Builder
public class OrderResponse {
    private Long id;

    private Long customerId;

    private String customerName;

    private String customerPhoneNumber;

    private Long restaurantId;

    private String restaurantName;

    private String restaurantPhoneNumber;

    private Long riderId;

    private String riderName;

    private String riderPhoneNumber;

    private String deliveryEvidenceUrl;

    private String deliveryNotes;

    private OrderStatus orderStatus;

    private List<OrderItemResponse> orderItems;

    private PaymentMethod paymentMethod;

    private String deliveryAddress;

    private BigDecimal subTotal;

    private BigDecimal deliveryFee;

    private BigDecimal totalAmount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
