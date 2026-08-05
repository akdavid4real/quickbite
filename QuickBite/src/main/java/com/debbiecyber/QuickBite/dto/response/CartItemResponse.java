package com.debbiecyber.QuickBite.dto.response;


import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class CartItemResponse {
    private Long cartItemId;

    private Long menuItemId;

    private Long restaurantId;

    private String restaurantName;

    private String itemName;

    private BigDecimal price;

    private Integer quantity;

    private BigDecimal subTotal;

    private String imageURL;

    private Boolean isAvailable;
}
