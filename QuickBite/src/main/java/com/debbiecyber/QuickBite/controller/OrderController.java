package com.debbiecyber.QuickBite.controller;


import com.debbiecyber.QuickBite.dto.response.APIResponse;
import com.debbiecyber.QuickBite.dto.response.OrderResponse;
import com.debbiecyber.QuickBite.dto.response.PageResponse;
import com.debbiecyber.QuickBite.dto.resquest.OrderRequest;
import com.debbiecyber.QuickBite.dto.resquest.DeliveryProofRequest;
import com.debbiecyber.QuickBite.dto.response.RiderSummaryResponse;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

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
    public ResponseEntity<APIResponse<PageResponse<OrderResponse>>> getMyOrders(@AuthenticationPrincipal UserDetails userDetails, @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<OrderResponse> orderResponse = orderService.getMyOrders(userDetails.getUsername(), pageable);

        return ResponseEntity.ok(APIResponse.success("Orders fetched successfully", orderResponse));
    }


    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER') and @providerAccess.canOperate(authentication.name)")
    public ResponseEntity<APIResponse<PageResponse<OrderResponse>>> getRestaurantOrders(@PathVariable Long restaurantId, @AuthenticationPrincipal UserDetails userDetails, @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<OrderResponse> orderResponse = orderService.getRestaurantOrders(restaurantId, userDetails.getUsername(), pageable);

        return ResponseEntity.ok(APIResponse.success("Restaurant orders fetched successfully", orderResponse));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<APIResponse<OrderResponse>> cancelOrder(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(APIResponse.success(
                "Order cancelled successfully",
                orderService.cancelOrder(id, userDetails.getUsername())
        ));
    }

    @GetMapping("/available-deliveries")
    @PreAuthorize("hasRole('RIDER') and @providerAccess.canOperate(authentication.name)")
    public ResponseEntity<APIResponse<PageResponse<OrderResponse>>> getAvailableDeliveries(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(APIResponse.success(
                "Available deliveries fetched successfully",
                orderService.getAvailableDeliveries(pageable)));
    }

    @GetMapping("/my-deliveries")
    @PreAuthorize("hasRole('RIDER') and @providerAccess.canOperate(authentication.name)")
    public ResponseEntity<APIResponse<PageResponse<OrderResponse>>> getMyDeliveries(@AuthenticationPrincipal UserDetails userDetails, @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(APIResponse.success(
                "Your deliveries fetched successfully",
                orderService.getRiderDeliveries(userDetails.getUsername(), pageable)));
    }


    @PatchMapping("/{id}/status")
    @PreAuthorize("(hasRole('RESTAURANT_OWNER') or hasRole('RIDER')) and @providerAccess.canOperate(authentication.name)")
    public ResponseEntity<APIResponse<OrderResponse>> updateOrderStatus(@PathVariable Long id, @RequestParam OrderStatus orderStatus, @AuthenticationPrincipal UserDetails userDetails) {
        OrderResponse orderResponse = orderService.updateOrderStatus(id, orderStatus, userDetails.getUsername());

        return ResponseEntity.ok(APIResponse.success("Order status updated successfully", orderResponse));
    }


    @PatchMapping("/{id}/assign-rider")
    @PreAuthorize("hasRole('RIDER') and @providerAccess.canOperate(authentication.name)")
    public ResponseEntity<APIResponse<OrderResponse>> assignRider(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        OrderResponse response = orderService.assignRider(id, userDetails.getUsername());

        return ResponseEntity.ok(APIResponse.success("Delivery accepted successfully", response));
    }

    @GetMapping("/rider/summary")
    @PreAuthorize("hasRole('RIDER') and @providerAccess.canOperate(authentication.name)")
    public ResponseEntity<APIResponse<RiderSummaryResponse>> getRiderSummary(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(APIResponse.success("Rider summary fetched successfully", orderService.getRiderSummary(userDetails.getUsername())));
    }

    @PatchMapping("/rider/availability")
    @PreAuthorize("hasRole('RIDER') and @providerAccess.canOperate(authentication.name)")
    public ResponseEntity<APIResponse<RiderSummaryResponse>> setRiderAvailability(@RequestParam boolean available, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(APIResponse.success("Rider availability updated", orderService.setRiderAvailability(userDetails.getUsername(), available)));
    }

    @PostMapping("/{id}/delivery-proof")
    @PreAuthorize("hasRole('RIDER') and @providerAccess.canOperate(authentication.name)")
    public ResponseEntity<APIResponse<OrderResponse>> submitDeliveryProof(@PathVariable Long id, @Valid @RequestBody DeliveryProofRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(APIResponse.success("Delivery completed with evidence", orderService.submitDeliveryProof(id, userDetails.getUsername(), request)));
    }
}
