package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Shop;
import com.ecommerce.backend.enums.ShopStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Integer> {

    Optional<Shop> findByUserId(Integer userId);

    List<Shop> findByStatus(ShopStatus status);

}