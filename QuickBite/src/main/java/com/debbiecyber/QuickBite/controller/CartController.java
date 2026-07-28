package com.debbiecyber.QuickBite.controller;


import com.debbiecyber.QuickBite.dto.response.APIResponse;
import com.debbiecyber.QuickBite.dto.response.CartResponse;
import com.debbiecyber.QuickBite.dto.resquest.CartItemRequest;
import com.debbiecyber.QuickBite.dto.resquest.CartQuantityRequest;
import com.debbiecyber.QuickBite.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<APIResponse<CartResponse>> addItemToCart(@Valid @RequestBody CartItemRequest cartItemRequest, @AuthenticationPrincipal UserDetails userDetails) {
        CartResponse cartResponse = cartService.addItemToCart(cartItemRequest, userDetails.getUsername());

        return ResponseEntity.ok(APIResponse.success("Item added successfully", cartResponse));
    }


    @PutMapping("/item/{cartItemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<APIResponse<CartResponse>> updateItemQuantity(@PathVariable Long cartItemId, @Valid @RequestBody CartQuantityRequest cartQuantityRequest, @AuthenticationPrincipal UserDetails userDetails) {
        CartResponse cartResponse = cartService.updateItemQuantity(cartItemId, cartQuantityRequest, userDetails.getUsername());

        return ResponseEntity.ok(APIResponse.success("item updated successfully", cartResponse));
    }


    @DeleteMapping("/item/{cartItemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<APIResponse<CartResponse>> removeItemFromCart(@PathVariable Long cartItemId, @AuthenticationPrincipal UserDetails userDetails) {
        CartResponse cartResponse = cartService.removeItemFromCart(cartItemId, userDetails.getUsername());

        return  ResponseEntity.ok(APIResponse.success("item removed from cart successfully", cartResponse));
    }


    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<APIResponse<CartResponse>> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        CartResponse cartResponse = cartService.getCart(userDetails.getUsername());

        return ResponseEntity.ok(APIResponse.success("Cart fetched successfully", cartResponse));
    }


    @DeleteMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<APIResponse<Void>> deleteCart(@AuthenticationPrincipal UserDetails userDetails) {
        cartService.clearCart(userDetails.getUsername());

        return ResponseEntity.ok(APIResponse.success("Cart cleared successfully"));
    }
}
