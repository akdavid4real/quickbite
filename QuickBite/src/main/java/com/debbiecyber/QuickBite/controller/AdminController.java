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
    public ResponseEntity<APIResponse<List<UserResponse>>> getAllUsers(@RequestParam(required = false)UserRole role) {
        List<UserResponse> userResponseList;
        if (role != null) {
            userResponseList = adminService.getUserByRole(role);
        } else {
            userResponseList = adminService.getAllUsers();
        }
        return ResponseEntity.ok(APIResponse.success("Users fetched successfully", userResponseList));
    }


    @GetMapping("/users/{id}")
    public ResponseEntity<APIResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse userResponse = adminService.getUserById(id);

        return  ResponseEntity.ok(success("User fetched successfully", userResponse));
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
    public ResponseEntity<APIResponse<List<RestaurantResponse>>> getAllRestaurants() {
        List<RestaurantResponse> restaurantResponseList = adminService.getAllRestaurants();

        return ResponseEntity.ok(APIResponse.success("Restaurants fetched successfully", restaurantResponseList));
    }


    @DeleteMapping("/restaurants/{id}")
    public ResponseEntity<APIResponse<Void>> deleteRestaurant(@PathVariable Long id) {
        adminService.deleteRestaurant(id);

        return ResponseEntity.ok(APIResponse.success("Restaurant deleted successfully"));
    }


    @GetMapping("/orders")
    public ResponseEntity<APIResponse<List<OrderResponse>>> getAllOrders(@RequestParam(required = false) OrderStatus orderStatus) {
        List<OrderResponse> orderResponseList;
        if (orderStatus != null) {
            orderResponseList = adminService.getOrdersByStatus(orderStatus);
        } else {
            orderResponseList = adminService.getAllOrders();
        }
        return  ResponseEntity.ok(APIResponse.success("Orders fetched successfully", orderResponseList));
    }


    @GetMapping("/reviews")
    public ResponseEntity<APIResponse<List<ReviewResponse>>> getAllReviews() {
        List<ReviewResponse> reviewResponseList = adminService.getAllReviews();

        return ResponseEntity.ok(APIResponse.success("Reviews fetched successfully", reviewResponseList));
    }


    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<APIResponse<Void>> deleteReview(@PathVariable Long id) {
        adminService.deleteReview(id);

        return ResponseEntity.ok(APIResponse.success("Review deleted successfully"));
    }
}
