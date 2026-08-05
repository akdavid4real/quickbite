package com.debbiecyber.QuickBite.service;


import com.debbiecyber.QuickBite.dto.response.OrderResponse;
import com.debbiecyber.QuickBite.dto.response.OrderItemResponse;
import com.debbiecyber.QuickBite.dto.response.PageResponse;
import com.debbiecyber.QuickBite.dto.resquest.OrderRequest;
import com.debbiecyber.QuickBite.dto.resquest.DeliveryProofRequest;
import com.debbiecyber.QuickBite.dto.response.RiderSummaryResponse;
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
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

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
    private final DeliveryProofRepository deliveryProofRepository;


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

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getCartItems()) {
            if (!cartItem.getMenuItem().getRestaurant().getId().equals(restaurant.getId())) {
                throw new BadRequestException("All cart items must belong to the same restaurant");
            }
            if (!cartItem.getMenuItem().getIsAvailable()) {
                throw new BadRequestException(cartItem.getMenuItem().getName() + " is currently unavailable");
            }
            subtotal = subtotal.add(cartItem.getMenuItem().getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        BigDecimal deliveryFee = BigDecimal.valueOf(
                mapService.calculateDeliveryFee(restaurant.getAddress(), orderRequest.getDeliveryAddress())
        );

        BigDecimal totalAmount = subtotal.add(deliveryFee);

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


    public PageResponse<OrderResponse> getMyOrders(String customerEmail, Pageable pageable) {
        User customer = userRepository.findByEmail(customerEmail).orElseThrow(()-> new ResourceNotFoundException("Customer not found"));
        return PageResponse.from(orderRepository.findByCustomerId(customer.getId(), pageable),
                order -> mapToResponse(order, orderItemRepository.findByOrderId(order.getId())));
    }


    public PageResponse<OrderResponse> getRestaurantOrders(Long restaurantId, String ownerEmail, Pageable pageable) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        if (!restaurant.getOwner().getEmail().equals(ownerEmail)) {
            throw new UnauthorizedException("You don't have permission to view this order");
        }

        return PageResponse.from(orderRepository.findByRestaurantId(restaurantId, pageable),
                order -> mapToResponse(order, orderItemRepository.findByOrderId(order.getId())));
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, String customerEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getCustomer().getEmail().equals(customerEmail)) {
            throw new ResourceNotFoundException("Order not found");
        }
        boolean cancellable = order.getOrderStatus() == OrderStatus.PENDING
                || (order.getOrderStatus() == OrderStatus.CONFIRMED
                && order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY);
        if (!cancellable) {
            throw new BadRequestException("This order can no longer be cancelled automatically. Contact support for help.");
        }
        order.setOrderStatus(OrderStatus.CANCELLED);
        Order updated = orderRepository.save(order);
        return mapToResponse(updated, orderItemRepository.findByOrderId(orderId));
    }

    public PageResponse<OrderResponse> getAvailableDeliveries(Pageable pageable) {
        return PageResponse.from(orderRepository.findByOrderStatusAndRiderIsNull(OrderStatus.READY_FOR_PICKUP, pageable),
                order -> mapToResponse(order, orderItemRepository.findByOrderId(order.getId())));
    }

    public PageResponse<OrderResponse> getRiderDeliveries(String riderEmail, Pageable pageable) {
        User rider = userRepository.findByEmail(riderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Rider not found"));
        return PageResponse.from(orderRepository.findByRiderId(rider.getId(), pageable),
                order -> mapToResponse(order, orderItemRepository.findByOrderId(order.getId())));
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
        if (isRider && newStatus == OrderStatus.DELIVERED) {
            throw new BadRequestException("Submit delivery evidence to complete this delivery");
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
        if (rider.getRole() != UserRole.RIDER || !Boolean.TRUE.equals(rider.getAvailableForDelivery())) {
            throw new BadRequestException("Set your rider status to available before accepting a delivery");
        }

        order.setRider(rider);
        order.setOrderStatus(OrderStatus.OUT_FOR_DELIVERY);
        Order updated = orderRepository.save(order);

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        return mapToResponse(updated, orderItems);
    }

    @Transactional
    public RiderSummaryResponse setRiderAvailability(String riderEmail, boolean available) {
        User rider = userRepository.findByEmail(riderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Rider not found"));
        rider.setAvailableForDelivery(available);
        userRepository.save(rider);
        return getRiderSummary(riderEmail);
    }

    public RiderSummaryResponse getRiderSummary(String riderEmail) {
        User rider = userRepository.findByEmail(riderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Rider not found"));
        List<Order> deliveries = orderRepository.findByRiderId(rider.getId());
        long completed = deliveries.stream().filter(order -> order.getOrderStatus() == OrderStatus.DELIVERED).count();
        long active = deliveries.stream().filter(order -> order.getOrderStatus() == OrderStatus.OUT_FOR_DELIVERY).count();
        BigDecimal earnings = deliveries.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.DELIVERED)
                .map(Order::getDeliveryFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return RiderSummaryResponse.builder().availableForDelivery(rider.getAvailableForDelivery())
                .activeDeliveries(active).completedDeliveries(completed).totalDeliveryEarnings(earnings).build();
    }

    @Transactional
    public OrderResponse submitDeliveryProof(Long orderId, String riderEmail, DeliveryProofRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (order.getRider() == null || !order.getRider().getEmail().equals(riderEmail)) {
            throw new ResourceNotFoundException("Delivery not found");
        }
        if (order.getOrderStatus() != OrderStatus.OUT_FOR_DELIVERY) {
            throw new BadRequestException("Only an out-for-delivery order can be completed");
        }
        if (deliveryProofRepository.existsByOrderId(orderId)) {
            throw new BadRequestException("Delivery evidence has already been submitted");
        }
        deliveryProofRepository.save(DeliveryProof.builder().order(order)
                .evidenceUrl(request.getEvidenceUrl()).notes(request.getNotes())
                .submittedAt(java.time.LocalDateTime.now()).build());
        order.setOrderStatus(OrderStatus.DELIVERED);
        return mapToResponse(orderRepository.save(order), orderItemRepository.findByOrderId(orderId));
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
            BigDecimal subtotal = orderItem.getPriceAtOrder()
                    .multiply(BigDecimal.valueOf(orderItem.getQuantity()));

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
        String riderPhoneNumber = null;
        if (order.getRider() != null) {
            riderId = order.getRider().getId();
            riderName = order.getRider().getName();
            riderPhoneNumber = order.getRider().getPhoneNumber();
        }
        DeliveryProof deliveryProof = deliveryProofRepository.findByOrderId(order.getId()).orElse(null);

        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomer().getId())
                .customerName(order.getCustomer().getName())
                .customerPhoneNumber(order.getCustomer().getPhoneNumber())
                .restaurantId(order.getRestaurant().getId())
                .restaurantName(order.getRestaurant().getName())
                .restaurantPhoneNumber(order.getRestaurant().getPhoneNumber())
                .riderId(riderId)
                .riderName(riderName)
                .riderPhoneNumber(riderPhoneNumber)
                .deliveryEvidenceUrl(deliveryProof == null ? null : deliveryProof.getEvidenceUrl())
                .deliveryNotes(deliveryProof == null ? null : deliveryProof.getNotes())
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
