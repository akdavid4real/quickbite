package com.debbiecyber.QuickBite.service;


import com.debbiecyber.QuickBite.dto.response.ReviewResponse;
import com.debbiecyber.QuickBite.dto.resquest.ReviewRequest;
import com.debbiecyber.QuickBite.entity.Order;
import com.debbiecyber.QuickBite.entity.Restaurant;
import com.debbiecyber.QuickBite.entity.Review;
import com.debbiecyber.QuickBite.entity.User;
import com.debbiecyber.QuickBite.exceptions.ResourceNotFoundException;
import com.debbiecyber.QuickBite.repository.OrderRepository;
import com.debbiecyber.QuickBite.repository.RestaurantRepository;
import com.debbiecyber.QuickBite.repository.ReviewRepository;
import com.debbiecyber.QuickBite.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;


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
        return mapToResponse(savedReview);
    }


    public List<ReviewResponse> getRestaurantReviews(Long restaurantId) {
        restaurantRepository.findById(restaurantId).orElseThrow(()-> new ResourceNotFoundException("Restaurant not found"));

        List<Review> reviewsList = reviewRepository.findReviewsByRestaurantIdAndCreatedAtDesc(restaurantId);

        List<ReviewResponse> reviewResponseList = new ArrayList<>();
        for (Review review : reviewsList) {
            reviewResponseList.add(mapToResponse(review));
        }
        return reviewResponseList;
    }


    public List<ReviewResponse> getMyReviews(String customerEmail) {
        User customer = userRepository.findByEmail(customerEmail).orElseThrow(()-> new ResourceNotFoundException("Customer not found"));

        List<Review> reviewList = reviewRepository.findByCustomerId(customer.getId());

        List<ReviewResponse> reviewResponseList = new ArrayList<>();
        for (Review review : reviewList) {
            reviewResponseList.add(mapToResponse(review));
        }
        return reviewResponseList;
    }


    public void deleteReview(Long reviewId, String customerEmail) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(()-> new ResourceNotFoundException("Review not found"));
        if (!review.getCustomer().getEmail().equals(customerEmail)) {
            throw new ResourceNotFoundException("You cannot delete this review");
        }
        reviewRepository.delete(review);
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
