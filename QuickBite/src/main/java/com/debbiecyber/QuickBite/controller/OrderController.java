package com.debbiecyber.QuickBite.controller;


import com.debbiecyber.QuickBite.dto.response.APIResponse;
import com.debbiecyber.QuickBite.dto.response.OrderResponse;
import com.debbiecyber.QuickBite.dto.resquest.OrderRequest;
import com.debbiecyber.QuickBite.enums.OrderStatus;
import com.debbiecyber.QuickBite.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService  orderService;


    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<APIResponse<OrderResponse>> placeOrder(@Valid @RequestBody OrderRequest orderRequest, @AuthenticationPrincipal UserDetails userDetails) {
        OrderResponse orderResponse = orderService.placeOrder(orderRequest, userDetails.getUsername());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(APIResponse.success("Order placed successfully", orderResponse));
    }


    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<OrderResponse>> getOrderById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        OrderResponse orderResponse = orderService.getOrderById(id, userDetails.getUsername());

        return ResponseEntity.ok(APIResponse.success("Order fetched successfully", orderResponse));
    }


    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<APIResponse<List<OrderResponse>>> getMyOrders(@AuthenticationPrincipal UserDetails userDetails) {
        List<OrderResponse> orderResponse = orderService.getMyOrders(userDetails.getUsername());

        return ResponseEntity.ok(APIResponse.success("Orders fetched successfully", orderResponse));
    }


    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<APIResponse<List<OrderResponse>>> getRestaurantOrders(@PathVariable Long restaurantId, @AuthenticationPrincipal UserDetails userDetails) {
        List<OrderResponse> orderResponse = orderService.getRestaurantOrders(restaurantId, userDetails.getUsername());

        return ResponseEntity.ok(APIResponse.success("Restaurant orders fetched successfully", orderResponse));
    }

    @GetMapping("/available-deliveries")
    @PreAuthorize("hasRole('RIDER')")
    public ResponseEntity<APIResponse<List<OrderResponse>>> getAvailableDeliveries() {
        return ResponseEntity.ok(APIResponse.success(
                "Available deliveries fetched successfully",
                orderService.getAvailableDeliveries()));
    }

    @GetMapping("/my-deliveries")
    @PreAuthorize("hasRole('RIDER')")
    public ResponseEntity<APIResponse<List<OrderResponse>>> getMyDeliveries(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(APIResponse.success(
                "Your deliveries fetched successfully",
                orderService.getRiderDeliveries(userDetails.getUsername())));
    }


    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('RIDER')")
    public ResponseEntity<APIResponse<OrderResponse>> updateOrderStatus(@PathVariable Long id, @RequestParam OrderStatus orderStatus, @AuthenticationPrincipal UserDetails userDetails) {
        OrderResponse orderResponse = orderService.updateOrderStatus(id, orderStatus, userDetails.getUsername());

        return ResponseEntity.ok(APIResponse.success("Order status updated successfully", orderResponse));
    }


    @PatchMapping("/{id}/assign-rider")
    @PreAuthorize("hasRole('RIDER')")
    public ResponseEntity<APIResponse<OrderResponse>> assignRider(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        OrderResponse response = orderService.assignRider(id, userDetails.getUsername());

        return ResponseEntity.ok(APIResponse.success("Delivery accepted successfully", response));
    }
}
