package com.debbiecyber.QuickBite.dto.response;


import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.math.BigDecimal;

@Data
@Builder
public class CartResponse {
    private Long cartId;

    private List<CartItemResponse> cartItems;

    private BigDecimal totalAmount;

    private Integer totalQuantity;
}
