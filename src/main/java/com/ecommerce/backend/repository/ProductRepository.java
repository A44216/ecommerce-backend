package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.enums.ProductStatus;
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
    Page<Product> findByShopIdAndCategoryIdAndIsDeletedFalse(
            Integer shopId, Integer categoryId, Pageable pageable
    );

    @Query("""
        SELECT p FROM Product p
        LEFT JOIN p.category c
        WHERE p.shop.id = :shopId
        AND (:isDeleted IS NULL OR p.isDeleted = :isDeleted)
        AND (:status IS NULL OR p.status = :status)
        AND (
            :keyword IS NULL OR
            LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
    """)
    Page<Product> filterProducts(
            @Param("shopId") Integer shopId,
            @Param("isDeleted") Boolean isDeleted,
            @Param("status") ProductStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}