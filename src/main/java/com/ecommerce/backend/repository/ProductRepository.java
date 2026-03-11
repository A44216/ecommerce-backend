package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    // tìm theo tên sản phẩm
    List<Product> findByNameContainingIgnoreCase(String keyword);

    // sản phẩm theo category
    List<Product> findByCategoryId(Integer categoryId);

    // sản phẩm theo shop
    List<Product> findByShopId(Integer shopId);

}