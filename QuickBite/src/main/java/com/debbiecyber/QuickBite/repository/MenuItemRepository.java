package com.debbiecyber.QuickBite.repository;

import com.debbiecyber.QuickBite.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByRestaurantId(Long restaurantId);

    List<MenuItem> findByRestaurantIdAndCategoryAndIsAvailableTrue(Long restaurantId, String category);

    boolean existsByNameAndRestaurantId(String name, Long restaurantId);


}
