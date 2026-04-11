package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @EntityGraph(attributePaths = {"images", "category", "shop"})
    Optional<Product> findByIdAndIsDeletedFalse(Integer id);

    @EntityGraph(attributePaths = {"images", "category", "shop"})
    Page<Product> findByShopIdAndIsDeletedFalse(Integer shopId, Pageable pageable);

    @EntityGraph(attributePaths = {"images", "category", "shop"})
    Page<Product> findByShopIdAndCategoryIdAndIsDeletedFalse(
            Integer shopId, Integer categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"images", "category", "shop"})
    Page<Product> findByShopIdAndIsDeletedTrue(Integer shopId, Pageable pageable);

    @Query("""
        SELECT p FROM Product p
        JOIN p.category c
        WHERE p.shop.id = :shopId
        AND p.isDeleted = false
        AND (
            LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
    """)
    Page<Product> searchByNameOrCategory(
            @Param("shopId") Integer shopId,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}