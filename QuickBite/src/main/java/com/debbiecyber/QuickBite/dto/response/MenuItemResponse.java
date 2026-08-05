package com.debbiecyber.QuickBite.dto.response;


import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class MenuItemResponse {

    private Long id;

    private Long restaurantId;

    private String name;

    private String description;

    private String category;

    private BigDecimal price;

    private String imageURL;

    private Boolean isAvailable;
}
