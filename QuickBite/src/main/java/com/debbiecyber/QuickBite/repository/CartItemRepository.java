package com.debbiecyber.QuickBite.repository;


import com.debbiecyber.QuickBite.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem>  findByCartIdAndMenuItemId(Long cartId, Long menuItemId);
}
