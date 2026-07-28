package com.debbiecyber.QuickBite.dto.resquest;


import com.debbiecyber.QuickBite.enums.CuisineType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RestaurantRequest {

    @NotBlank(message = "Restaurant name is required")
    private String name;

    private String description;

    @NotNull(message = "Cuisine type is required")
    private CuisineType cuisineType;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Phone numer is required")
    private String phoneNumber;

    private String logoURl;
}
