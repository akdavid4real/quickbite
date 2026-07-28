package com.debbiecyber.QuickBite.repository;


import com.debbiecyber.QuickBite.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r WHERE r.restaurant.id = :restaurantId ORDER BY r.createdAt DESC")
    List<Review> findReviewsByRestaurantIdAndCreatedAtDesc(Long restaurantId);

    List<Review> findByCustomerId(Long customerId);

    boolean existsByCustomerIdAndOrderId(Long customerId, Long orderId);

    Optional<Review> findByCustomerIdAndOrderId(Long customerId, Long orderId);
}
