package com.debbiecyber.QuickBite.service;


import com.debbiecyber.QuickBite.dto.response.OrderResponse;
import com.debbiecyber.QuickBite.dto.response.OrderItemResponse;
import com.debbiecyber.QuickBite.dto.resquest.OrderRequest;
import com.debbiecyber.QuickBite.entity.*;
import com.debbiecyber.QuickBite.enums.OrderStatus;
import com.debbiecyber.QuickBite.enums.PaymentMethod;
import com.debbiecyber.QuickBite.enums.UserRole;
import com.debbiecyber.QuickBite.exceptions.BadRequestException;
import com.debbiecyber.QuickBite.exceptions.ResourceNotFoundException;
import com.debbiecyber.QuickBite.exceptions.UnauthorizedException;
import com.debbiecyber.QuickBite.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MapService mapService;


    @Transactional
    public OrderResponse placeOrder(OrderRequest orderRequest, String customerEmail) {

        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Cart cart = cartRepository.findCartByCustomerId(customer.getId())
                .orElseThrow(() -> new BadRequestException(
                        "You have no cart. Please add items to cart before checking out."));

        if (cart.getCartItems().isEmpty()) {throw new BadRequestException(
                "Your cart is empty. Please add items to cart before checking out");
        }

        Restaurant restaurant = cart.getCartItems().get(0).getMenuItem().getRestaurant();
        if (!restaurant.getIsOpen()) {throw new BadRequestException("Sorry " + restaurant.getName() + " is not open. Please try again later");
        }

        double subtotal = 0.0;
        for (CartItem cartItem : cart.getCartItems()) {
            if (!cartItem.getMenuItem().getRestaurant().getId().equals(restaurant.getId())) {
                throw new BadRequestException("All cart items must belong to the same restaurant");
            }
            if (!cartItem.getMenuItem().getIsAvailable()) {
                throw new BadRequestException(cartItem.getMenuItem().getName() + " is currently unavailable");
            }
            subtotal += cartItem.getMenuItem().getPrice() * cartItem.getQuantity();
        }

        double deliveryFee = mapService.calculateDeliveryFee(restaurant.getAddress(), orderRequest.getDeliveryAddress());

        double totalAmount = subtotal + deliveryFee;

        Order order = Order.builder()
                .customer(customer)
                .restaurant(restaurant)
                .orderStatus(OrderStatus.PENDING)
                .paymentMethod(orderRequest.getPaymentMethod())
                .deliveryAddress(orderRequest.getDeliveryAddress())
                .subTotal(subtotal)
                .deliveryFee(deliveryFee)
                .totalAmount(totalAmount)
                .build();

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .menuItem(cartItem.getMenuItem())
                    .itemName(cartItem.getMenuItem().getName())
                    .quantity(cartItem.getQuantity())
                    .priceAtOrder(cartItem.getMenuItem().getPrice())
                    .build();
            orderItems.add(orderItem);
        }

        orderItemRepository.saveAll(orderItems);
        cart.getCartItems().clear();
        cartRepository.save(cart);

        return mapToResponse(savedOrder, orderItems);
    }


    public OrderResponse getOrderById(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        boolean isCustomer = order.getCustomer().getEmail().equals(userEmail);
        boolean isOwner = order.getRestaurant().getOwner().getEmail().equals(userEmail);
        boolean isRider = order.getRider() != null && order.getRider().getEmail().equals(userEmail);
        if (!isCustomer && !isOwner && !isRider) {
            throw new UnauthorizedException("You don't have permission to view this order");
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        return mapToResponse(order, orderItems);
    }


    public List<OrderResponse> getMyOrders(String customerEmail) {
        User customer = userRepository.findByEmail(customerEmail).orElseThrow(()-> new ResourceNotFoundException("Customer not found"));

        List<Order> orders = orderRepository.findByCustomerId(customer.getId());

        List<OrderResponse> orderResponseList = new ArrayList<>();
        for (Order order : orders) {
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
            orderResponseList.add(mapToResponse(order, orderItems));
        }
        return orderResponseList;
    }


    public List<OrderResponse> getRestaurantOrders(Long restaurantId, String ownerEmail) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        if (!restaurant.getOwner().getEmail().equals(ownerEmail)) {
            throw new UnauthorizedException("You don't have permission to view this order");
        }

        List<Order> orders = orderRepository.findByRestaurantId(restaurantId);

        List<OrderResponse> orderResponseList = new ArrayList<>();
        for (Order order : orders) {
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
            orderResponseList.add(mapToResponse(order, orderItems));
        }
        return orderResponseList;
    }

    public List<OrderResponse> getAvailableDeliveries() {
        List<Order> orders = orderRepository.findByOrderStatusAndRiderIsNull(OrderStatus.READY_FOR_PICKUP);
        List<OrderResponse> responses = new ArrayList<>();
        for (Order order : orders) {
            responses.add(mapToResponse(order, orderItemRepository.findByOrderId(order.getId())));
        }
        return responses;
    }

    public List<OrderResponse> getRiderDeliveries(String riderEmail) {
        User rider = userRepository.findByEmail(riderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Rider not found"));
        List<OrderResponse> responses = new ArrayList<>();
        for (Order order : orderRepository.findByRiderId(rider.getId())) {
            responses.add(mapToResponse(order, orderItemRepository.findByOrderId(order.getId())));
        }
        return responses;
    }


    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus, String userEmail) {

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order  not found"));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isOwner = order.getRestaurant().getOwner().getEmail().equals(userEmail);
        boolean isRider = order.getRider() != null && order.getRider().getEmail().equals(userEmail);

        if (!isOwner && !isRider) {throw new UnauthorizedException("You do not have permission to update this order.");
        }
        validateStatusTransition(order, newStatus, user.getRole());

        order.setOrderStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        return mapToResponse(updatedOrder, orderItems);
    }


    @Transactional
    public OrderResponse assignRider(Long orderId, String riderEmail) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId));

        if (!order.getOrderStatus().equals(OrderStatus.READY_FOR_PICKUP)) {
            throw new BadRequestException(
                    "This order is not ready for pickup yet.");
        }
        if (order.getRider() != null) {
            throw new BadRequestException("This order has already been assigned to a rider");
        }

        User rider = userRepository.findByEmail(riderEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Rider not found with email: " + riderEmail));

        order.setRider(rider);
        order.setOrderStatus(OrderStatus.OUT_FOR_DELIVERY);
        Order updated = orderRepository.save(order);

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        return mapToResponse(updated, orderItems);
    }


    private void validateStatusTransition(Order order, OrderStatus newStatus, UserRole actorRole) {
        OrderStatus currentStatus = order.getOrderStatus();
        if (currentStatus == newStatus) {
            return;
        }

        boolean allowed;
        if (actorRole == UserRole.RESTAURANT_OWNER) {
            allowed = switch (currentStatus) {
                case PENDING -> order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY
                        && (newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.CANCELLED);
                case CONFIRMED -> newStatus == OrderStatus.PREPARING
                        || (order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY
                        && newStatus == OrderStatus.CANCELLED);
                case PREPARING -> newStatus == OrderStatus.READY_FOR_PICKUP;
                default -> false;
            };
        } else if (actorRole == UserRole.RIDER) {
            allowed = currentStatus == OrderStatus.OUT_FOR_DELIVERY
                    && newStatus == OrderStatus.DELIVERED;
        } else {
            allowed = false;
        }

        if (!allowed) {
            throw new BadRequestException(
                    "Invalid order status transition from " + currentStatus + " to " + newStatus
            );
        }
    }


    private OrderResponse mapToResponse(Order order, List<OrderItem> orderItems) {

        List<OrderItemResponse> itemResponses = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            double subtotal = orderItem.getPriceAtOrder()
                    * orderItem.getQuantity();

            OrderItemResponse itemResponse = OrderItemResponse.builder()
                    .id(orderItem.getId())
                    .itemName(orderItem.getItemName())
                    .price(orderItem.getPriceAtOrder())
                    .quantity(orderItem.getQuantity())
                    .subTotal(subtotal)
                    .build();

            itemResponses.add(itemResponse);
        }

        Long riderId = null;
        String riderName = null;
        if (order.getRider() != null) {
            riderId = order.getRider().getId();
            riderName = order.getRider().getName();
        }

        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomer().getId())
                .customerName(order.getCustomer().getName())
                .restaurantId(order.getRestaurant().getId())
                .restaurantName(order.getRestaurant().getName())
                .riderId(riderId)
                .riderName(riderName)
                .orderItems(itemResponses)
                .orderStatus(order.getOrderStatus())
                .paymentMethod(order.getPaymentMethod())
                .deliveryAddress(order.getDeliveryAddress())
                .subTotal(order.getSubTotal())
                .deliveryFee(order.getDeliveryFee())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
