package com.debbiecyber.QuickBite.dto.response;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardResponse {
    private Long totalUsers;

    private Long totalCustomers;

    private Long totalRestaurantOwners;

    private Long totalRestaurants;

    private Long totalRiders;

    private Long totalOrders;

    private Long totalDeliveredOrders;

    private Long totalCancelledOrders;

    private Long totalSuccessfulPayments;
}
