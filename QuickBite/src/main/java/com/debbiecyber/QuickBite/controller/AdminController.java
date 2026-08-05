package com.debbiecyber.QuickBite.controller;


import com.debbiecyber.QuickBite.dto.response.*;
import com.debbiecyber.QuickBite.dto.resquest.UserRequest;
import com.debbiecyber.QuickBite.enums.OrderStatus;
import com.debbiecyber.QuickBite.enums.UserRole;
import com.debbiecyber.QuickBite.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import java.util.List;

import static com.debbiecyber.QuickBite.dto.response.APIResponse.success;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;


    @GetMapping("/dashboard")
    public ResponseEntity<APIResponse<DashboardResponse>> getDashboard() {
        DashboardResponse dashboardResponse = adminService.getDashboard();

        return ResponseEntity.ok(success("Dashboard loaded successfully", dashboardResponse));
    }


    @GetMapping("/users")
    public ResponseEntity<APIResponse<PageResponse<UserResponse>>> getAllUsers(@RequestParam(required = false) UserRole role, @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(APIResponse.success("Users fetched successfully", adminService.getUsers(role, pageable)));
    }


    @GetMapping("/users/{id}")
    public ResponseEntity<APIResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse userResponse = adminService.getUserById(id);

        return  ResponseEntity.ok(success("User fetched successfully", userResponse));
    }

    @GetMapping("/providers/pending-approval")
    public ResponseEntity<APIResponse<PageResponse<UserResponse>>> getProvidersPendingApproval(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(APIResponse.success(
                "Providers pending approval fetched successfully",
                adminService.getProvidersPendingApproval(pageable)
        ));
    }

    @PatchMapping("/users/{id}/approve-provider")
    public ResponseEntity<APIResponse<UserResponse>> approveProvider(@PathVariable Long id) {
        return ResponseEntity.ok(APIResponse.success("Provider approved successfully", adminService.approveProvider(id)));
    }

    @PatchMapping("/users/{id}/suspend")
    public ResponseEntity<APIResponse<UserResponse>> suspendUser(@PathVariable Long id) {
        return ResponseEntity.ok(APIResponse.success("User suspended successfully", adminService.suspendUser(id)));
    }

    @PatchMapping("/users/{id}/reactivate")
    public ResponseEntity<APIResponse<UserResponse>> reactivateUser(@PathVariable Long id) {
        return ResponseEntity.ok(APIResponse.success("User reactivated successfully", adminService.reactivateUser(id)));
    }


    @PutMapping("/users/{id}")
    public ResponseEntity<APIResponse<UserResponse>> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest userRequest) {
        UserResponse userResponse = adminService.updateUser(id, userRequest);

        return ResponseEntity.ok(APIResponse.success("User updated successfully", userResponse));
    }


    @DeleteMapping("/users/{id}")
    public ResponseEntity<APIResponse<Void>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);

        return ResponseEntity.ok(APIResponse.success("User deleted successfully"));
    }


    @GetMapping("/restaurants")
    public ResponseEntity<APIResponse<PageResponse<RestaurantResponse>>> getAllRestaurants(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(APIResponse.success("Restaurants fetched successfully", adminService.getRestaurants(pageable)));
    }


    @DeleteMapping("/restaurants/{id}")
    public ResponseEntity<APIResponse<Void>> deleteRestaurant(@PathVariable Long id) {
        adminService.deleteRestaurant(id);

        return ResponseEntity.ok(APIResponse.success("Restaurant deleted successfully"));
    }


    @GetMapping("/orders")
    public ResponseEntity<APIResponse<PageResponse<OrderResponse>>> getAllOrders(@RequestParam(required = false) OrderStatus orderStatus, @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(APIResponse.success("Orders fetched successfully", adminService.getOrders(orderStatus, pageable)));
    }


    @GetMapping("/reviews")
    public ResponseEntity<APIResponse<PageResponse<ReviewResponse>>> getAllReviews(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(APIResponse.success("Reviews fetched successfully", adminService.getReviews(pageable)));
    }

    @GetMapping("/restaurants/pending-approval")
    public ResponseEntity<APIResponse<PageResponse<RestaurantResponse>>> getRestaurantsPendingApproval(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(APIResponse.success("Restaurants pending approval fetched successfully", adminService.getRestaurantsPendingApproval(pageable)));
    }

    @PatchMapping("/restaurants/{id}/approve")
    public ResponseEntity<APIResponse<RestaurantResponse>> approveRestaurant(@PathVariable Long id) {
        return ResponseEntity.ok(APIResponse.success("Restaurant approved successfully", adminService.approveRestaurant(id)));
    }

    @PatchMapping("/restaurants/{id}/reject")
    public ResponseEntity<APIResponse<RestaurantResponse>> rejectRestaurant(@PathVariable Long id) {
        return ResponseEntity.ok(APIResponse.success("Restaurant rejected", adminService.rejectRestaurant(id)));
    }

    @PatchMapping("/orders/{id}/resolve")
    public ResponseEntity<APIResponse<OrderResponse>> resolveOrder(@PathVariable Long id, @RequestParam OrderStatus orderStatus) {
        return ResponseEntity.ok(APIResponse.success("Order resolved successfully", adminService.resolveOrder(id, orderStatus)));
    }


    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<APIResponse<Void>> deleteReview(@PathVariable Long id) {
        adminService.deleteReview(id);

        return ResponseEntity.ok(APIResponse.success("Review deleted successfully"));
    }
}
