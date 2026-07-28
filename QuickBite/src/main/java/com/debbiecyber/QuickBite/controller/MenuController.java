package com.debbiecyber.QuickBite.controller;


import com.debbiecyber.QuickBite.dto.response.APIResponse;
import com.debbiecyber.QuickBite.dto.response.MenuItemResponse;
import com.debbiecyber.QuickBite.dto.resquest.MenuItemRequest;
import com.debbiecyber.QuickBite.service.MenuItemService;
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
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuItemService menuItemService;


    @PostMapping("/{restaurantId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<APIResponse<MenuItemResponse>> addMenuItem(@PathVariable Long restaurantId, @Valid @RequestBody MenuItemRequest menuItemRequest, @AuthenticationPrincipal UserDetails userDetails) {
        MenuItemResponse menuItemResponse = menuItemService.addMenuItem(restaurantId, menuItemRequest, userDetails.getUsername());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(APIResponse.success("Menu item added successfully", menuItemResponse));
    }


    @PutMapping("/{menuItemId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<APIResponse<MenuItemResponse>> updateMenuItem(@PathVariable Long menuItemId, @Valid @RequestBody MenuItemRequest menuItemRequest, @AuthenticationPrincipal UserDetails userDetails) {
        MenuItemResponse menuItemResponse = menuItemService.updateMenuItem(menuItemId, menuItemRequest, userDetails.getUsername());

        return ResponseEntity.ok(APIResponse.success("Menu item updated successfully", menuItemResponse));
    }


    @GetMapping("/{restaurantId}/all")
    public ResponseEntity<APIResponse<List<MenuItemResponse>>> getAllMenuItems(@PathVariable Long restaurantId) {
        List<MenuItemResponse> menuItemResponseList = menuItemService.getFullMenu(restaurantId);

        return ResponseEntity.ok(APIResponse.success("Menu items fetched successfully", menuItemResponseList));
    }


    @GetMapping("/{restaurantId}/category")
    public ResponseEntity<APIResponse<List<MenuItemResponse>>> getAllMenuItemsByCategory(@PathVariable Long restaurantId, @RequestParam String name) {
        List<MenuItemResponse> menuItemResponseList = menuItemService.getMenuByCategory(restaurantId, name);

        return ResponseEntity.ok(APIResponse.success("Menu items by " + name + " category, fetched successfully", menuItemResponseList));
    }


    @PatchMapping("/{menuItemId}/hideOrShow")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<APIResponse<MenuItemResponse>> hideOrShow(@PathVariable Long menuItemId, @AuthenticationPrincipal UserDetails userDetails) {

        MenuItemResponse menuItemResponseList = menuItemService.hideOrShow(menuItemId, userDetails.getUsername());

        return ResponseEntity.ok(APIResponse.success("Menu item visibility updated successfully", menuItemResponseList));
    }


    @DeleteMapping("/{menuItemId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public  ResponseEntity<APIResponse<MenuItemResponse>> deleteMenuItem(@PathVariable Long menuItemId, @AuthenticationPrincipal UserDetails userDetails) {
        menuItemService.deleteMenuItem(menuItemId, userDetails.getUsername());

        return ResponseEntity.ok(APIResponse.success("Menu item deleted successfully"));
    }
}
