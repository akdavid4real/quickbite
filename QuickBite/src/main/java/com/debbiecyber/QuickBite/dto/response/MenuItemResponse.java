package com.debbiecyber.QuickBite.dto.response;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MenuItemResponse {

    private Long id;

    private Long restaurantId;

    private String name;

    private String description;

    private String category;

    private Double price;

    private String imageURL;

    private Boolean isAvailable;
}
