package com.debbiecyber.QuickBite.service;


import com.debbiecyber.QuickBite.dto.response.CartItemResponse;
import com.debbiecyber.QuickBite.dto.response.CartResponse;
import com.debbiecyber.QuickBite.dto.resquest.CartItemRequest;
import com.debbiecyber.QuickBite.dto.resquest.CartQuantityRequest;
import com.debbiecyber.QuickBite.entity.Cart;
import com.debbiecyber.QuickBite.entity.CartItem;
import com.debbiecyber.QuickBite.entity.MenuItem;
import com.debbiecyber.QuickBite.entity.User;
import com.debbiecyber.QuickBite.exceptions.BadRequestException;
import com.debbiecyber.QuickBite.exceptions.ResourceNotFoundException;
import com.debbiecyber.QuickBite.repository.CartItemRepository;
import com.debbiecyber.QuickBite.repository.CartRepository;
import com.debbiecyber.QuickBite.repository.MenuItemRepository;
import com.debbiecyber.QuickBite.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;


    @Transactional
    public CartResponse addItemToCart(CartItemRequest cartItemRequest, String customerEmail) {
        User customer = userRepository.findByEmail(customerEmail).orElseThrow(()->new ResourceNotFoundException("Customer not found"));

        MenuItem menuItem = menuItemRepository.findById(cartItemRequest.getMenuItemId()).orElseThrow(()->new ResourceNotFoundException("Menu item not found"));
        if (!menuItem.getIsAvailable()) {
            throw new BadRequestException("This menu item is currently unavailable");
        }
        if (!menuItem.getRestaurant().getIsOpen()) {
            throw new BadRequestException("This restaurant is currently closed");
        }

        Cart cart = cartRepository.findCartByCustomerId(customer.getId()).orElseGet(()-> {
                Cart newCart = Cart.builder()
                        .customer(customer)
                        .build();
                return cartRepository.save(newCart);
        });

        boolean containsAnotherRestaurant = cart.getCartItems().stream()
                .anyMatch(item -> !item.getMenuItem().getRestaurant().getId()
                        .equals(menuItem.getRestaurant().getId()));
        if (containsAnotherRestaurant) {
            throw new BadRequestException("A cart can only contain items from one restaurant");
        }

        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndMenuItemId(cart.getId(), menuItem.getId());

        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + cartItemRequest.getQuantity());
            cartItemRepository.save(cartItem);
        } else {
            CartItem newCartItem = CartItem.builder()
                    .cart(cart)
                    .menuItem(menuItem)
                    .quantity(cartItemRequest.getQuantity())
                    .build();
            cart.getCartItems().add(newCartItem);
            cartRepository.save(cart);
        }
        return mapToResponse(cart);
    }


    @Transactional
    public CartResponse removeItemFromCart(Long cartItemId, String customerEmail) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(()->new ResourceNotFoundException("Cart item not found"));

        if (!cartItem.getCart().getCustomer().getEmail().equals(customerEmail)) {
            throw new BadRequestException("This cart doesnt belong to you");
        }

        Cart cart = cartItem.getCart();
        cart.getCartItems().remove(cartItem);
        cartRepository.save(cart);

        return mapToResponse(cart);
    }


    public CartResponse getCart(String customerEmail) {
        User customer = userRepository.findByEmail(customerEmail).orElseThrow(()->new ResourceNotFoundException("Customer not found"));

        Cart cart = cartRepository.findCartByCustomerId(customer.getId()).orElse(null);
        if (cart == null) {
            return CartResponse.builder()
                    .cartId(null)
                    .cartItems(List.of())
                    .totalAmount(BigDecimal.ZERO)
                    .totalQuantity(0)
                    .build();
        }
        return mapToResponse(cart);
    }


    @Transactional
    public CartResponse updateItemQuantity(Long cartItemId, CartQuantityRequest cartQuantityRequest, String customerEmail) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(()-> new ResourceNotFoundException("Cart item not found"));

        if (!cartItem.getCart().getCustomer().getEmail().equals(customerEmail)) {
            throw new BadRequestException("This cart doesnt belong to you");
        }

        if (cartQuantityRequest.getQuantity() == 0) {
            Cart cart = cartItem.getCart();
            cart.getCartItems().remove(cartItem);
            cartRepository.save(cart);
            return mapToResponse(cart);
        }
        cartItem.setQuantity(cartQuantityRequest.getQuantity());
        cartItemRepository.save(cartItem);

        return mapToResponse(cartItem.getCart());
    }


    @Transactional
    public void clearCart(String customerEmail) {
        User customer = userRepository.findByEmail(customerEmail).orElseThrow(()->new ResourceNotFoundException("Customer not found"));

        Cart cart = cartRepository.findCartByCustomerId(customer.getId()).orElseThrow(()->new ResourceNotFoundException("Cart not found"));
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }


    private CartResponse mapToResponse(Cart cart) {

        List<CartItemResponse> cartItemResponseList = new ArrayList<>();

        for (CartItem cartItem : cart.getCartItems()) {
            BigDecimal subtotal = cartItem.getMenuItem().getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            CartItemResponse itemResponse = CartItemResponse.builder()
                    .cartItemId(cartItem.getId())
                    .menuItemId(cartItem.getMenuItem().getId())
                    .restaurantId(cartItem.getMenuItem().getRestaurant().getId())
                    .restaurantName(cartItem.getMenuItem().getRestaurant().getName())
                    .itemName(cartItem.getMenuItem().getName())
                    .price(cartItem.getMenuItem().getPrice())
                    .quantity(cartItem.getQuantity())
                    .subTotal(subtotal)
                    .imageURL(cartItem.getMenuItem().getImageURL())
                    .isAvailable(cartItem.getMenuItem().getIsAvailable())
                    .build();

            cartItemResponseList.add(itemResponse);
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItemResponse item : cartItemResponseList) {
            totalAmount = totalAmount.add(item.getSubTotal());
        }

        int totalItems = 0;
        for (CartItem cartItem : cart.getCartItems()) {
            totalItems += cartItem.getQuantity();
        }

        return CartResponse.builder()
                .cartId(cart.getId())
                .cartItems(cartItemResponseList)
                .totalAmount(totalAmount)
                .totalQuantity(totalItems)
                .build();
    }
}
