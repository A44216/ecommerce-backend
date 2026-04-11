package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @EntityGraph(attributePaths = {"images", "category", "shop"})
    List<Product> findByIsDeletedFalse();

    @EntityGraph(attributePaths = {"images", "category", "shop"})
    Optional<Product> findByIdAndIsDeletedFalse(Integer id);

    @EntityGraph(attributePaths = {"images", "category", "shop"})
    List<Product> findByNameContainingIgnoreCaseAndIsDeletedFalse(String keyword);

    @EntityGraph(attributePaths = {"images", "category", "shop"})
    List<Product> findByCategoryIdAndIsDeletedFalse(Integer categoryId);

    @EntityGraph(attributePaths = {"images", "category", "shop"})
    List<Product> findByShopIdAndIsDeletedFalse(Integer shopId);

    @EntityGraph(attributePaths = {"images", "category"})
    List<Product> findByIsDeletedTrue();

}