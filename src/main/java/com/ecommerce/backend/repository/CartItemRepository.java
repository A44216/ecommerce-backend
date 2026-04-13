package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    // tất cả item trong cart
    List<CartItem> findByCartId(Integer cartId);

    // tìm item theo cart + product
    Optional<CartItem> findByCartIdAndProductId(Integer cartId, Integer productId);
}