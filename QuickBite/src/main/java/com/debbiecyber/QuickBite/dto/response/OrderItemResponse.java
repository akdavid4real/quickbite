package com.debbiecyber.QuickBite.dto.response;


import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {
    private Long id;

    private String itemName;

    private String imageURL;

    private BigDecimal price;

    private Integer quantity;

    private BigDecimal subTotal;
}
