package com.debbiecyber.QuickBite.service;


import com.debbiecyber.QuickBite.dto.response.RestaurantResponse;
import com.debbiecyber.QuickBite.dto.resquest.RestaurantRequest;
import com.debbiecyber.QuickBite.entity.Restaurant;
import com.debbiecyber.QuickBite.entity.User;
import com.debbiecyber.QuickBite.enums.CuisineType;
import com.debbiecyber.QuickBite.enums.UserRole;
import com.debbiecyber.QuickBite.exceptions.BadRequestException;
import com.debbiecyber.QuickBite.exceptions.ResourceNotFoundException;
import com.debbiecyber.QuickBite.exceptions.UnauthorizedException;
import com.debbiecyber.QuickBite.repository.RestaurantRepository;
import com.debbiecyber.QuickBite.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    @Transactional
    public RestaurantResponse createRestaurant(RestaurantRequest restaurantRequest, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail).orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + ownerEmail));

        if (!owner.getRole().equals(UserRole.RESTAURANT_OWNER)) {
            throw new UnauthorizedException("Only restaurant owners can access this.");
        }

        if (restaurantRepository.existsByNameAndOwnerId(restaurantRequest.getName(), owner.getId())) {
            throw new BadRequestException("You already have a restaurant named: " + restaurantRequest.getName());
        }

        Restaurant restaurant = Restaurant.builder()
                .owner(owner)
                .name(restaurantRequest.getName())
                .description(restaurantRequest.getDescription())
                .cuisineType(restaurantRequest.getCuisineType())
                .address(restaurantRequest.getAddress())
                .phoneNumber(restaurantRequest.getPhoneNumber())
                .logoURL(restaurantRequest.getLogoURl())
                .build();

        Restaurant createdRestaurant = restaurantRepository.save(restaurant);
        return mapToResponse(createdRestaurant);
    }


    @Transactional
    public RestaurantResponse updateRestaurant(Long restaurantId, RestaurantRequest restaurantRequest, String ownerEmail) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(()-> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));
        if (!restaurant.getOwner().getEmail().equals(ownerEmail)){
            throw new UnauthorizedException("You dont have permission to update this restaurant because you are not the owner of this restaurant.");
        }

        restaurant.setName(restaurantRequest.getName());
        restaurant.setDescription(restaurantRequest.getDescription());
        restaurant.setCuisineType(restaurantRequest.getCuisineType());
        restaurant.setAddress(restaurantRequest.getAddress());
        restaurant.setPhoneNumber(restaurantRequest.getPhoneNumber());
        restaurant.setLogoURL(restaurantRequest.getLogoURl());

        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        return mapToResponse(updatedRestaurant);
    }


    public List<RestaurantResponse> getAllOpenRestaurants() {
        List<Restaurant> restaurants = restaurantRepository.findByIsOpenTrue();

        List<RestaurantResponse> responseList = new ArrayList<>();
        for (Restaurant restaurant : restaurants) {
            responseList.add(mapToResponse(restaurant));
        }
        return responseList;
    }


    public List<RestaurantResponse> getRestaurantByCuisine(CuisineType cuisineType) {
        List<Restaurant> restaurants = restaurantRepository
                .findByCuisineTypeAndIsOpenTrue(cuisineType);

        List<RestaurantResponse> responseList = new ArrayList<>();
        for (Restaurant restaurant : restaurants) {
            responseList.add(mapToResponse(restaurant));
        }
        return responseList;
    }


    public RestaurantResponse getRestaurantById(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant not found with id: " + restaurantId));
        return mapToResponse(restaurant);
    }


    public List<RestaurantResponse> searchRestaurantByName(String name) {
        List<Restaurant> restaurants = restaurantRepository.findByName(name);

        List<RestaurantResponse> responseList = new ArrayList<>();
        for (Restaurant restaurant : restaurants) {
            responseList.add(mapToResponse(restaurant));
        }
        return responseList;
    }


    public List<RestaurantResponse> getMyRestaurants(String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with email " + ownerEmail + " not found"));

        List<Restaurant> restaurants = restaurantRepository
                .findByOwnerId(owner.getId());

        List<RestaurantResponse> responseList = new ArrayList<>();
        for (Restaurant restaurant : restaurants) {
            responseList.add(mapToResponse(restaurant));
        }
        return responseList;
    }


    @Transactional
    public RestaurantResponse openOrClose(Long restaurantId, String ownerEmail) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(()-> new ResourceNotFoundException("Restaurant with id " + restaurantId + " not found"));

        if (!restaurant.getOwner().getEmail().equals(ownerEmail)) {
            throw new UnauthorizedException("You do not have permission to update the status of this restaurant");
        }

        restaurant.setIsOpen(!restaurant.getIsOpen());
        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        return mapToResponse(updatedRestaurant);
    }


    @Transactional
    public void deleteRestaurant(Long restaurantId, String ownerEmail) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(()-> new ResourceNotFoundException("Restaurant with id " + restaurantId + " not found"));

        if(!restaurant.getOwner().getEmail().equals(ownerEmail)) {
            throw new UnauthorizedException("You do not have permission to delete this restaurant");
        }
        restaurantRepository.delete(restaurant);
    }


    private RestaurantResponse mapToResponse(Restaurant restaurant) {
        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .ownerId(restaurant.getOwner().getId())
                .ownerName(restaurant.getOwner().getName())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .cuisineType(restaurant.getCuisineType())
                .address(restaurant.getAddress())
                .phoneNumber(restaurant.getPhoneNumber())
                .logoURL(restaurant.getLogoURL())
                .rating(restaurant.getRating())
                .isOpen(restaurant.getIsOpen())
                .build();
    }
}
