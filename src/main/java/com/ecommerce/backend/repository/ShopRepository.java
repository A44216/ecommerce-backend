package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShopRepository extends JpaRepository<Shop, Integer> {

    List<Shop> findByUserId(Integer userId);

    List<Shop> findByStatus(String status);

}