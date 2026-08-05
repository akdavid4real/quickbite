package com.debbiecyber.QuickBite.repository;


import com.debbiecyber.QuickBite.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;


@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r WHERE r.restaurant.id = :restaurantId ORDER BY r.createdAt DESC")
    List<Review> findReviewsByRestaurantIdAndCreatedAtDesc(Long restaurantId);

    @Query("SELECT r FROM Review r WHERE r.restaurant.id = :restaurantId")
    Page<Review> findByRestaurantId(Long restaurantId, Pageable pageable);

    List<Review> findByCustomerId(Long customerId);

    Page<Review> findByCustomerId(Long customerId, Pageable pageable);

    boolean existsByCustomerIdAndOrderId(Long customerId, Long orderId);

    Optional<Review> findByCustomerIdAndOrderId(Long customerId, Long orderId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.restaurant.id = :restaurantId")
    Double findAverageRatingByRestaurantId(Long restaurantId);
}
