package com.debbiecyber.QuickBite.service;

import com.debbiecyber.QuickBite.dto.resquest.ReviewRequest;
import com.debbiecyber.QuickBite.entity.Order;
import com.debbiecyber.QuickBite.entity.Restaurant;
import com.debbiecyber.QuickBite.entity.Review;
import com.debbiecyber.QuickBite.entity.User;
import com.debbiecyber.QuickBite.enums.OrderStatus;
import com.debbiecyber.QuickBite.exceptions.BadRequestException;
import com.debbiecyber.QuickBite.repository.OrderRepository;
import com.debbiecyber.QuickBite.repository.RestaurantRepository;
import com.debbiecyber.QuickBite.repository.ReviewRepository;
import com.debbiecyber.QuickBite.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReviewServiceTests {
    @Test
    void rejectsReviewBeforeDelivery() {
        Fixture fixture = fixture(OrderStatus.PREPARING);

        assertThrows(BadRequestException.class, () -> fixture.service.leaveAReview(fixture.request, "customer@test.local"));
    }

    @Test
    void deliveredReviewRecalculatesRestaurantRating() {
        Fixture fixture = fixture(OrderStatus.DELIVERED);
        when(fixture.reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(fixture.reviewRepository.findAverageRatingByRestaurantId(3L)).thenReturn(4.5);

        fixture.service.leaveAReview(fixture.request, "customer@test.local");

        assertEquals(4.5, fixture.restaurant.getRating());
        verify(fixture.restaurantRepository).save(fixture.restaurant);
    }

    private Fixture fixture(OrderStatus status) {
        UserRepository userRepository = mock(UserRepository.class);
        RestaurantRepository restaurantRepository = mock(RestaurantRepository.class);
        OrderRepository orderRepository = mock(OrderRepository.class);
        ReviewRepository reviewRepository = mock(ReviewRepository.class);
        ReviewService service = new ReviewService(reviewRepository, orderRepository, restaurantRepository, userRepository);

        User customer = User.builder().id(1L).name("Customer").email("customer@test.local").build();
        Restaurant restaurant = Restaurant.builder().id(3L).name("Kitchen").owner(User.builder().id(2L).build()).rating(0.0).build();
        Order order = Order.builder().id(4L).customer(customer).restaurant(restaurant).orderStatus(status).build();
        ReviewRequest request = new ReviewRequest();
        request.setOrderId(4L); request.setRestaurantId(3L); request.setRating(5); request.setComment("Great meal");

        when(userRepository.findByEmail("customer@test.local")).thenReturn(Optional.of(customer));
        when(restaurantRepository.findById(3L)).thenReturn(Optional.of(restaurant));
        when(orderRepository.findById(4L)).thenReturn(Optional.of(order));
        when(reviewRepository.existsByCustomerIdAndOrderId(1L, 4L)).thenReturn(false);
        return new Fixture(service, reviewRepository, restaurantRepository, restaurant, request);
    }

    private record Fixture(ReviewService service, ReviewRepository reviewRepository,
                           RestaurantRepository restaurantRepository, Restaurant restaurant,
                           ReviewRequest request) {}
}
