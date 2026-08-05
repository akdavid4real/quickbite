package com.debbiecyber.QuickBite.repository;


import com.debbiecyber.QuickBite.entity.Order;
import com.debbiecyber.QuickBite.enums.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Override
    @EntityGraph(attributePaths = {"customer", "restaurant", "restaurant.owner", "rider"})
    Optional<Order> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"customer", "restaurant", "restaurant.owner", "rider"})
    List<Order> findAll();

    @Override
    @EntityGraph(attributePaths = {"customer", "restaurant", "restaurant.owner", "rider"})
    Page<Order> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"customer", "restaurant", "restaurant.owner", "rider"})
    List<Order> findByCustomerId(Long customerId);

    @EntityGraph(attributePaths = {"customer", "restaurant", "restaurant.owner", "rider"})
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    @EntityGraph(attributePaths = {"customer", "restaurant", "restaurant.owner", "rider"})
    List<Order> findByRestaurantId(Long restaurantId);

    @EntityGraph(attributePaths = {"customer", "restaurant", "restaurant.owner", "rider"})
    Page<Order> findByRestaurantId(Long restaurantId, Pageable pageable);

    @EntityGraph(attributePaths = {"customer", "restaurant", "restaurant.owner", "rider"})
    List<Order> findByRiderId(Long riderId);

    @EntityGraph(attributePaths = {"customer", "restaurant", "restaurant.owner", "rider"})
    Page<Order> findByRiderId(Long riderId, Pageable pageable);

    @EntityGraph(attributePaths = {"customer", "restaurant", "restaurant.owner", "rider"})
    List<Order> findByOrderStatus(OrderStatus orderStatus);

    @EntityGraph(attributePaths = {"customer", "restaurant", "restaurant.owner", "rider"})
    Page<Order> findByOrderStatus(OrderStatus orderStatus, Pageable pageable);

    @EntityGraph(attributePaths = {"customer", "restaurant", "restaurant.owner", "rider"})
    List<Order> findByOrderStatusAndRiderIsNull(OrderStatus orderStatus);

    @EntityGraph(attributePaths = {"customer", "restaurant", "restaurant.owner", "rider"})
    Page<Order> findByOrderStatusAndRiderIsNull(OrderStatus orderStatus, Pageable pageable);

    @EntityGraph(attributePaths = {"customer", "restaurant", "restaurant.owner", "rider"})
    List<Order> findByRestaurantIdAndOrderStatus(Long restaurantId, OrderStatus orderStatus);

    @EntityGraph(attributePaths = {"customer", "restaurant", "restaurant.owner", "rider"})
    List<Order> findByCustomerIdAndOrderStatus(Long customerId, OrderStatus orderStatus);

    boolean existsByCustomerIdAndRestaurantIdAndOrderStatus(Long customerId, Long restaurantId, OrderStatus status);
}
