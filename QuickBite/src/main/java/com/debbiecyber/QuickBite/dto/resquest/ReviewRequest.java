package com.debbiecyber.QuickBite.dto.resquest;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewRequest {
    @NotNull(message = "Restaurant id is required")
    private Long restaurantId;

    @NotNull(message = "Order id is required")
    private Long orderId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least one")
    @Max(value = 5, message = "Rating must not exceed 5")
    private Integer rating;

    private String comment;
}
