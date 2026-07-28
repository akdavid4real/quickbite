package com.debbiecyber.QuickBite.dto.response;


import com.debbiecyber.QuickBite.enums.CuisineType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RestaurantResponse {

    private Long id;

    private Long ownerId;

    private String ownerName;

    private String name;

    private String description;

    private CuisineType cuisineType;

    private String address;

    private String phoneNumber;

    private String logoURL;

    private Double rating;

    private Boolean isOpen;


}
