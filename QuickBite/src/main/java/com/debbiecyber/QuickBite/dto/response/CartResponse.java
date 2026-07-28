package com.debbiecyber.QuickBite.dto.response;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CartResponse {
    private Long cartId;

    private List<CartItemResponse> cartItems;

    private Double totalAmount;

    private Integer totalQuantity;
}
