package com.debbiecyber.QuickBite.dto.response;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemResponse {
    private Long id;

    private String itemName;

    private Double price;

    private Integer quantity;

    private Double subTotal;
}
