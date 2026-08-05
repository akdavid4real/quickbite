package com.debbiecyber.QuickBite.repository;


import com.debbiecyber.QuickBite.entity.Restaurant;
import com.debbiecyber.QuickBite.enums.CuisineType;
import com.debbiecyber.QuickBite.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByOwnerId(Long ownerId);

    Page<Restaurant> findByOwnerId(Long ownerId, Pageable pageable);

    @Query("SELECT r FROM Restaurant r WHERE LOWER(r.name) LIKE LOWER (CONCAT('%', :name, '%'))")
    List<Restaurant> findByName(String name);

    @Query("SELECT r FROM Restaurant r WHERE LOWER(r.name) LIKE LOWER (CONCAT('%', :name, '%'))")
    Page<Restaurant> findByName(String name, Pageable pageable);

    List<Restaurant> findByIsOpenTrue();

    Page<Restaurant> findByIsOpenTrueAndVerificationStatus(VerificationStatus verificationStatus, Pageable pageable);

    List<Restaurant> findByCuisineTypeAndIsOpenTrue(CuisineType cuisineType);

    Page<Restaurant> findByCuisineTypeAndIsOpenTrueAndVerificationStatus(CuisineType cuisineType, VerificationStatus verificationStatus, Pageable pageable);

    List<Restaurant> findByVerificationStatus(VerificationStatus verificationStatus);

    Page<Restaurant> findByVerificationStatus(VerificationStatus verificationStatus, Pageable pageable);

    boolean existsByNameAndOwnerId(String name, Long ownerId);
}
