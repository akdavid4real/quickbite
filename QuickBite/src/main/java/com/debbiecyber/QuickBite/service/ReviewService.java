package com.debbiecyber.QuickBite.service;


import com.debbiecyber.QuickBite.dto.response.ReviewResponse;
import com.debbiecyber.QuickBite.dto.response.PageResponse;
import com.debbiecyber.QuickBite.dto.resquest.ReviewRequest;
import com.debbiecyber.QuickBite.entity.Order;
import com.debbiecyber.QuickBite.entity.Restaurant;
import com.debbiecyber.QuickBite.entity.Review;
import com.debbiecyber.QuickBite.entity.User;
import com.debbiecyber.QuickBite.exceptions.ResourceNotFoundException;
import com.debbiecyber.QuickBite.exceptions.BadRequestException;
import com.debbiecyber.QuickBite.enums.OrderStatus;
import com.debbiecyber.QuickBite.repository.OrderRepository;
import com.debbiecyber.QuickBite.repository.RestaurantRepository;
import com.debbiecyber.QuickBite.repository.ReviewRepository;
import com.debbiecyber.QuickBite.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;


    @Transactional
    public ReviewResponse leaveAReview(ReviewRequest reviewRequest, String customerEmail) {
        User customer = userRepository.findByEmail(customerEmail).orElseThrow(()-> new ResourceNotFoundException("Customer not found"));

        Restaurant restaurant = restaurantRepository.findById(reviewRequest.getRestaurantId()).orElseThrow(()-> new ResourceNotFoundException("Restaurant not found"));

        Order order = orderRepository.findById(reviewRequest.getOrderId()).orElseThrow(()-> new ResourceNotFoundException("Order not found"));

        if (!order.getCustomer().getEmail().equals(customerEmail)) {
            throw new ResourceNotFoundException("This order doesn't belong to you");
        }
        if (!order.getRestaurant().getId().equals(reviewRequest.getRestaurantId())) {
            throw new ResourceNotFoundException("This order is not from this restaurant");
        }
        if (order.getOrderStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException("You can review an order after it has been delivered");
        }

        if (reviewRepository.existsByCustomerIdAndOrderId(customer.getId(), order.getId())) {
            throw new ResourceNotFoundException("You have already reviewed this order");
        }
        Review review = Review.builder()
                .customer(customer)
                .restaurant(restaurant)
                .order(order)
                .rating(reviewRequest.getRating())
                .comment(reviewRequest.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);
        reviewRepository.flush();
        updateRestaurantRating(restaurant);
        return mapToResponse(savedReview);
    }


    public PageResponse<ReviewResponse> getRestaurantReviews(Long restaurantId, Pageable pageable) {
        restaurantRepository.findById(restaurantId).orElseThrow(()-> new ResourceNotFoundException("Restaurant not found"));
        return PageResponse.from(reviewRepository.findByRestaurantId(restaurantId, pageable), this::mapToResponse);
    }


    public PageResponse<ReviewResponse> getMyReviews(String customerEmail, Pageable pageable) {
        User customer = userRepository.findByEmail(customerEmail).orElseThrow(()-> new ResourceNotFoundException("Customer not found"));

        return PageResponse.from(reviewRepository.findByCustomerId(customer.getId(), pageable), this::mapToResponse);
    }


    @Transactional
    public void deleteReview(Long reviewId, String customerEmail) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(()-> new ResourceNotFoundException("Review not found"));
        if (!review.getCustomer().getEmail().equals(customerEmail)) {
            throw new ResourceNotFoundException("You cannot delete this review");
        }
        Restaurant restaurant = review.getRestaurant();
        reviewRepository.delete(review);
        reviewRepository.flush();
        updateRestaurantRating(restaurant);
    }

    private void updateRestaurantRating(Restaurant restaurant) {
        Double average = reviewRepository.findAverageRatingByRestaurantId(restaurant.getId());
        restaurant.setRating(average == null ? 0.0 : Math.round(average * 10.0) / 10.0);
        restaurantRepository.save(restaurant);
    }


    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .customerId(review.getCustomer().getId())
                .customerName(review.getCustomer().getName())
                .restaurantId(review.getRestaurant().getId())
                .restaurantName(review.getRestaurant().getName())
                .orderId(review.getOrder().getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
