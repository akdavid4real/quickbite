package com.debbiecyber.QuickBite.dto.response;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponse {
    private Long id;

    private Long customerId;

    private String customerName;

    private Long restaurantId;

    private String restaurantName;

    private Long orderId;

    private Integer rating;

    private String comment;

    private LocalDateTime  createdAt;
}
