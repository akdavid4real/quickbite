package com.debbiecyber.QuickBite.repository;


import com.debbiecyber.QuickBite.entity.Order;
import com.debbiecyber.QuickBite.enums.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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

    @EntityGraph(attributePaths = {"customer", "restaurant", "restaurant.owner", "rider"})
    List<Order> findByCustomerId(Long customerId);

    @EntityGraph(attributePaths = {"customer", "restaurant", "restaurant.owner", "rider"})
    List<Order> findByRestaurantId(Long restaurantId);

    @EntityGraph(attributePaths = {"customer", "restaurant", "restaurant.owner", "rider"})
    List<Order> findByRiderId(Long riderId);

    @EntityGraph(attributePaths = {"customer", "restaurant", "restaurant.owner", "rider"})
    List<Order> findByOrderStatus(OrderStatus orderStatus);

    @EntityGraph(attributePaths = {"customer", "restaurant", "restaurant.owner", "rider"})
    List<Order> findByOrderStatusAndRiderIsNull(OrderStatus orderStatus);

    @EntityGraph(attributePaths = {"customer", "restaurant", "restaurant.owner", "rider"})
    List<Order> findByRestaurantIdAndOrderStatus(Long restaurantId, OrderStatus orderStatus);

    @EntityGraph(attributePaths = {"customer", "restaurant", "restaurant.owner", "rider"})
    List<Order> findByCustomerIdAndOrderStatus(Long customerId, OrderStatus orderStatus);

    boolean existsByCustomerIdAndRestaurantIdAndOrderStatus(Long customerId, Long restaurantId, OrderStatus status);
}
