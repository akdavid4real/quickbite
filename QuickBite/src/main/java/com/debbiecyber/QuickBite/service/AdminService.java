package com.debbiecyber.QuickBite.service;


import com.debbiecyber.QuickBite.dto.response.*;
import com.debbiecyber.QuickBite.dto.response.OrderItemResponse;
import com.debbiecyber.QuickBite.dto.resquest.UserRequest;
import com.debbiecyber.QuickBite.entity.*;
import com.debbiecyber.QuickBite.enums.OrderStatus;
import com.debbiecyber.QuickBite.enums.PaymentStatus;
import com.debbiecyber.QuickBite.enums.UserRole;
import com.debbiecyber.QuickBite.exceptions.ResourceNotFoundException;
import com.debbiecyber.QuickBite.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;


    public DashboardResponse getDashboard() {
        long totalUsers = userRepository.count();
        long totalCustomers = userRepository.findByRole(UserRole.CUSTOMER).size();
        long totalOwners = userRepository.findByRole(UserRole.RESTAURANT_OWNER).size();
        long totalRiders = userRepository.findByRole(UserRole.RIDER).size();

        long totalRestaurants = restaurantRepository.count();
        long totalOrders = orderRepository.count();
        long totalDeliveredOrders = orderRepository.findByOrderStatus(OrderStatus.DELIVERED).size();
        long totalCancelledOrders = orderRepository.findByOrderStatus(OrderStatus.CANCELLED).size();
        long totalSuccessfulPayments = paymentRepository.findByStatus(PaymentStatus.SUCCESS).size();

        return DashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalCustomers(totalCustomers)
                .totalRestaurantOwners(totalOwners)
                .totalRiders(totalRiders)
                .totalRestaurants(totalRestaurants)
                .totalOrders(totalOrders)
                .totalDeliveredOrders(totalDeliveredOrders)
                .totalCancelledOrders(totalCancelledOrders)
                .totalSuccessfulPayments(totalSuccessfulPayments)
                .build();
    }


    public List<UserResponse> getAllUsers() {
        List<User> userList = userRepository.findAll();
        List<UserResponse> userResponseList = new ArrayList<>();
        for (User users : userList) {
            userResponseList.add(mapUserToResponse(users));
        }
        return userResponseList;
    }


    public List<UserResponse> getUserByRole(UserRole role) {
        List<User> userList = userRepository.findByRole(role);
        List<UserResponse> userResponseList = new ArrayList<>();
        for (User users : userList) {
            userResponseList.add(mapUserToResponse(users));
        }
        return userResponseList;
    }


    public UserResponse getUserById(Long userId) {
        User users = userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User not found"));
        return mapUserToResponse(users);
    }


    @Transactional
    public UserResponse updateUser(Long userId, UserRequest userRequest) {
        User user = userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User not found"));
        user.setName(userRequest.getName());
        user.setPhoneNumber(userRequest.getPhoneNumber());
        user.setAddress(userRequest.getAddress());
        user.setRole(userRequest.getRole());

        User updatedUser = userRepository.save(user);
        return mapUserToResponse(updatedUser);
    }


    @Transactional
    public void deleteUser(Long userId) {
        User users = userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User not found"));
        userRepository.delete(users);
    }


    public List<RestaurantResponse> getAllRestaurants() {
        List<Restaurant> restaurantList = restaurantRepository.findAll();
        List<RestaurantResponse> restaurantResponseList = new ArrayList<>();
        for (Restaurant restaurants : restaurantList) {
            restaurantResponseList.add(mapRestaurantToResponse(restaurants));
        }
        return restaurantResponseList;
    }


    @Transactional
    public void deleteRestaurant(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(()-> new ResourceNotFoundException("Restaurant not found"));
        restaurantRepository.delete(restaurant);
    }


    public List<OrderResponse> getAllOrders() {
        List<Order> orderList = orderRepository.findAll();
        List<OrderResponse>  orderResponseList = new ArrayList<>();
        for (Order orders : orderList) {
            List<OrderItem> orderItemList = orderItemRepository.findByOrderId(orders.getId());
            orderResponseList.add(mapOrderToResponse(orders, orderItemList));
        }
        return orderResponseList;
    }


    public List<OrderResponse> getOrdersByStatus(OrderStatus orderStatus) {
        List<Order> orderList = orderRepository.findByOrderStatus(orderStatus);
        List<OrderResponse>  orderResponseList = new ArrayList<>();
        for (Order orders : orderList) {
            List<OrderItem> orderItemList = orderItemRepository.findByOrderId(orders.getId());
            orderResponseList.add(mapOrderToResponse(orders, orderItemList));
        }
        return orderResponseList;
    }


    public List<ReviewResponse> getAllReviews() {
        List<Review> reviewList = reviewRepository.findAll();
        List<ReviewResponse> reviewResponseList = new ArrayList<>();
        for (Review reviews : reviewList) {
            reviewResponseList.add(mapReviewToResponse(reviews));
        }
        return reviewResponseList;
    }


    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(()-> new ResourceNotFoundException("Review not found"));
        reviewRepository.delete(review);
    }


    private ReviewResponse mapReviewToResponse(Review reviews) {
        return ReviewResponse.builder()
                .id(reviews.getId())
                .customerId(reviews.getCustomer().getId())
                .customerName(reviews.getCustomer().getName())
                .restaurantId(reviews.getRestaurant().getId())
                .restaurantName(reviews.getRestaurant().getName())
                .orderId(reviews.getOrder().getId())
                .rating(reviews.getRating())
                .comment(reviews.getComment())
                .createdAt(reviews.getCreatedAt())
                .build();
    }


    private UserResponse mapUserToResponse(User users) {
        return UserResponse.builder()
                .id(users.getId())
                .name(users.getName())
                .email(users.getEmail())
                .phoneNumber(users.getPhoneNumber())
                .role(users.getRole())
                .address(users.getAddress())
                .createdAt(users.getCreatedAt())
                .build();
    }


    private  RestaurantResponse mapRestaurantToResponse(Restaurant restaurants) {
        return RestaurantResponse.builder()
                .id(restaurants.getId())
                .ownerId(restaurants.getOwner().getId())
                .ownerName(restaurants.getOwner().getName())
                .name(restaurants.getName())
                .description(restaurants.getDescription())
                .cuisineType(restaurants.getCuisineType())
                .address(restaurants.getAddress())
                .phoneNumber(restaurants.getPhoneNumber())
                .logoURL(restaurants.getLogoURL())
                .rating(restaurants.getRating())
                .isOpen(restaurants.getIsOpen())
                .build();
    }


    private OrderResponse mapOrderToResponse(Order order, List<OrderItem> orderItemLists) {
        List<OrderItemResponse> orderItemResponseList = new ArrayList<>();

        for (OrderItem orderItems : orderItemLists) {
            double subTotal = orderItems.getPriceAtOrder() * orderItems.getQuantity();
            orderItemResponseList.add(OrderItemResponse.builder()
                    .id(orderItems.getId())
                    .itemName(orderItems.getItemName())
                    .price(orderItems.getPriceAtOrder())
                    .quantity(orderItems.getQuantity())
                    .subTotal(subTotal)
                    .build());
        }

        Long riderId = null;
        String riderName = null;
        if (order.getRider() != null) {
            riderId = order.getRider().getId();
            riderName =  order.getRider().getName();
        }

        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomer().getId())
                .customerName(order.getCustomer().getName())
                .restaurantId(order.getRestaurant().getId())
                .restaurantName(order.getRestaurant().getName())
                .riderId(riderId)
                .riderName(riderName)
                .orderItems(orderItemResponseList)
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
