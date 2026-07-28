package com.debbiecyber.QuickBite.service;


import com.debbiecyber.QuickBite.dto.response.MenuItemResponse;
import com.debbiecyber.QuickBite.dto.resquest.MenuItemRequest;
import com.debbiecyber.QuickBite.entity.MenuItem;
import com.debbiecyber.QuickBite.entity.Restaurant;
import com.debbiecyber.QuickBite.exceptions.BadRequestException;
import com.debbiecyber.QuickBite.exceptions.ResourceNotFoundException;
import com.debbiecyber.QuickBite.exceptions.UnauthorizedException;
import com.debbiecyber.QuickBite.repository.MenuItemRepository;
import com.debbiecyber.QuickBite.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;


    public MenuItemResponse addMenuItem(Long restaurantId, MenuItemRequest menuItemRequest, String ownerEmail) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(()-> new ResourceNotFoundException("Restaurant not found"));

        if (!restaurant.getOwner().getEmail().equals(ownerEmail)) {
            throw new UnauthorizedException("You are not authorized to perform this task");
        }

        if (menuItemRepository.existsByNameAndRestaurantId(menuItemRequest.getName(), restaurantId)) {
            throw new BadRequestException("Menu item already exists");
        }

        MenuItem menuItem = MenuItem.builder()
                .restaurant(restaurant)
                .name(menuItemRequest.getName())
                .description(menuItemRequest.getDescription())
                .category(menuItemRequest.getCategory())
                .price(menuItemRequest.getPrice())
                .imageURL(menuItemRequest.getImageURL())
                .build();

        MenuItem savedMenuItem = menuItemRepository.save(menuItem);
        return mapToResponse(savedMenuItem);
    }


    public MenuItemResponse updateMenuItem(Long menuItemId, MenuItemRequest menuItemRequest, String ownerEmail) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId).orElseThrow(()->new ResourceNotFoundException("MenuItem not found"));

        if (!menuItem.getRestaurant().getOwner().getEmail().equals(ownerEmail)) {
            throw new UnauthorizedException("You are not authorized to perform this task");
        }

        menuItem.setName(menuItemRequest.getName());
        menuItem.setDescription(menuItemRequest.getDescription());
        menuItem.setCategory(menuItemRequest.getCategory());
        menuItem.setPrice(menuItemRequest.getPrice());
        menuItem.setImageURL(menuItemRequest.getImageURL());

        MenuItem updatedMenuItem = menuItemRepository.save(menuItem);
        return mapToResponse(updatedMenuItem);
    }


    public List<MenuItemResponse> getFullMenu(Long restaurantId) {
        restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        List<MenuItem> menuItems = menuItemRepository.findByRestaurantId(restaurantId);

        List<MenuItemResponse> responseList = new ArrayList<>();
        for (MenuItem menuItem : menuItems) {
            responseList.add(mapToResponse(menuItem));
        }
        return responseList;
    }


    public List<MenuItemResponse> getMenuByCategory(Long restaurantId, String category) {
        restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        List<MenuItem> menuItems = menuItemRepository
                .findByRestaurantIdAndCategoryAndIsAvailableTrue(restaurantId, category);

        List<MenuItemResponse> responseList = new ArrayList<>();
        for (MenuItem menuItem : menuItems) {
            responseList.add(mapToResponse(menuItem));
        }
        return responseList;
    }


    public MenuItemResponse hideOrShow(Long menuItemId, String ownerEmail) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId).orElseThrow(() -> new ResourceNotFoundException("MenuItem not found"));

        if (!menuItem.getRestaurant().getOwner().getEmail().equals(ownerEmail)) {
            throw new UnauthorizedException("You are not authorized to perform this task");
        }

        menuItem.setIsAvailable(!menuItem.getIsAvailable());

        MenuItem updatedMenuItem = menuItemRepository.save(menuItem);
        return mapToResponse(updatedMenuItem);
    }


    public void deleteMenuItem(Long menuItemId, String ownerEmail) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId).orElseThrow(()->new ResourceNotFoundException("MenuItem not found"));

        if(!menuItem.getRestaurant().getOwner().getEmail().equals(ownerEmail)) {
            throw new UnauthorizedException("You are not authorized to perform this task");
        }
        menuItemRepository.delete(menuItem);
    }


    private MenuItemResponse mapToResponse(MenuItem menuItem) {
        return MenuItemResponse.builder()
                .id(menuItem.getId())
                .restaurantId(menuItem.getRestaurant().getId())
                .name(menuItem.getName())
                .description(menuItem.getDescription())
                .category(menuItem.getCategory())
                .price(menuItem.getPrice())
                .imageURL(menuItem.getImageURL())
                .isAvailable(menuItem.getIsAvailable())
                .build();
    }


}
