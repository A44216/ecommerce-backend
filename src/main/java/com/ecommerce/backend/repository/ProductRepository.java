package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findByIsDeletedFalse();

    Optional<Product> findByIdAndIsDeletedFalse(Integer id);

    List<Product> findByNameContainingIgnoreCaseAndIsDeletedFalse(String keyword);

    List<Product> findByCategoryIdAndIsDeletedFalse(Integer categoryId);

    List<Product> findByShopIdAndIsDeletedFalse(Integer shopId);

}