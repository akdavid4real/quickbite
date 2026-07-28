package com.debbiecyber.QuickBite.controller;


import com.debbiecyber.QuickBite.dto.response.APIResponse;
import com.debbiecyber.QuickBite.dto.response.RestaurantResponse;
import com.debbiecyber.QuickBite.dto.resquest.RestaurantRequest;
import com.debbiecyber.QuickBite.enums.CuisineType;
import com.debbiecyber.QuickBite.service.RestaurantService;
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
@RequestMapping("api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;


    @PostMapping
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<APIResponse<RestaurantResponse>> createRestaurant(@Valid @RequestBody RestaurantRequest restaurantRequest, @AuthenticationPrincipal UserDetails userDetails) {
        RestaurantResponse restaurantResponse = restaurantService.createRestaurant(restaurantRequest, userDetails.getUsername());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(APIResponse.success("Restaurant created successfully", restaurantResponse));
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<APIResponse<RestaurantResponse>> updateRestaurant(@PathVariable Long id, @Valid @RequestBody RestaurantRequest restaurantRequest, @AuthenticationPrincipal UserDetails userDetails) {
        RestaurantResponse restaurantResponse = restaurantService.updateRestaurant(id, restaurantRequest, userDetails.getUsername());

        return ResponseEntity.ok(APIResponse.success("Restaurant updated successfully", restaurantResponse));
    }


    @GetMapping
    public ResponseEntity<APIResponse<List<RestaurantResponse>>> getAllOpenRestaurants() {
        List<RestaurantResponse> restaurantResponsesList = restaurantService.getAllOpenRestaurants();

        return ResponseEntity.ok(APIResponse.success("Restaurants fetched successfully", restaurantResponsesList));
    }


    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<RestaurantResponse>> getRestaurantById(@PathVariable Long id) {
        RestaurantResponse restaurantResponse = restaurantService.getRestaurantById(id);

        return ResponseEntity.ok(APIResponse.success("Restaurant found successfully", restaurantResponse));
    }


    @GetMapping("/cuisine/{cuisineType}")
    public ResponseEntity<APIResponse<List<RestaurantResponse>>> getRestaurantsByCuisine(@PathVariable CuisineType cuisineType) {
        List<RestaurantResponse> restaurantResponseList = restaurantService.getRestaurantByCuisine(cuisineType);

        return ResponseEntity.ok(APIResponse.success("Restaurants fetched successfully", restaurantResponseList));
    }


    @GetMapping("/search")
    public ResponseEntity<APIResponse<List<RestaurantResponse>>> searchRestaurants(@RequestParam String name) {
        List<RestaurantResponse> restaurantResponseList = restaurantService.searchRestaurantByName(name);

        return ResponseEntity.ok(APIResponse.success("Restaurants according to search, fetched successfully", restaurantResponseList));
    }


    @GetMapping("/my-restaurants")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<APIResponse<List<RestaurantResponse>>> getMyRestaurants(@AuthenticationPrincipal UserDetails userDetails) {
        List<RestaurantResponse> restaurantResponseList = restaurantService.getMyRestaurants(userDetails.getUsername());

        return ResponseEntity.ok(APIResponse.success("Your restaurants fetched successfully!", restaurantResponseList));
    }


    @PatchMapping("{id}/openOrClose")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<APIResponse<RestaurantResponse>> openOrClose(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        RestaurantResponse restaurantResponse = restaurantService.openOrClose(id, userDetails.getUsername());

        return ResponseEntity.ok(APIResponse.success("Restaurant status updated successfully", restaurantResponse));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<APIResponse<Void>> deleteRestaurant(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        restaurantService.deleteRestaurant(id, userDetails.getUsername());

        return ResponseEntity.ok(APIResponse.success("Restaurant deleted successfully"));
    }
}
