package com.debbiecyber.QuickBite.repository;


import com.debbiecyber.QuickBite.entity.Restaurant;
import com.debbiecyber.QuickBite.enums.CuisineType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByOwnerId(Long ownerId);

    @Query("SELECT r FROM Restaurant r WHERE LOWER(r.name) LIKE LOWER (CONCAT('%', :name, '%'))")
    List<Restaurant> findByName(String name);

    List<Restaurant> findByIsOpenTrue();

    List<Restaurant> findByCuisineTypeAndIsOpenTrue(CuisineType cuisineType);

    boolean existsByNameAndOwnerId(String name, Long ownerId);
}
