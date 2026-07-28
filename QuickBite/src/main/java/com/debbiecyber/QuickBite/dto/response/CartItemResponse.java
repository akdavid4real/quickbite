package com.debbiecyber.QuickBite.dto.response;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemResponse {
    private Long cartItemId;

    private Long menuItemId;

    private Long restaurantId;

    private String restaurantName;

    private String itemName;

    private Double price;

    private Integer quantity;

    private Double subTotal;

    private String imageURL;

    private Boolean isAvailable;
}
