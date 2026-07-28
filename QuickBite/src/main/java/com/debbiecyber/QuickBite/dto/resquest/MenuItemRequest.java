package com.debbiecyber.QuickBite.dto.resquest;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MenuItemRequest {

    @NotBlank(message = "Item name is required")
    private String name;

    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Price is required")
    @Min(value = 1, message = "Price must be greater then 0")
    private Double price;

    private String imageURL;
}
