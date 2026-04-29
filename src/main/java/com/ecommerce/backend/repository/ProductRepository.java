package com.ecommerce.backend.repository;

import com.ecommerce.backend.dto.responses.ProductAutocompleteResponse;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @EntityGraph(attributePaths = {"images", "category", "shop"})
    Optional<Product> findByIdAndIsDeletedFalse(Integer id);

    Integer countByShopIdAndIsDeletedFalse(Integer shopId);

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
            LOWER(p.productCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        ORDER BY
            CASE
                WHEN LOWER(p.productCode) LIKE LOWER(CONCAT(:keyword, '%')) THEN 0
                WHEN LOWER(p.name) LIKE LOWER(CONCAT(:keyword, '%')) THEN 1
                ELSE 2
            END,
            p.createdAt DESC
    """)
    Page<Product> filterProducts(
            @Param("shopId") Integer shopId,
            @Param("isDeleted") Boolean isDeleted,
            @Param("status") ProductStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query(value = """
        SELECT p FROM Product p
        LEFT JOIN FETCH p.category c
        LEFT JOIN FETCH p.shop s
        WHERE (:shopId IS NULL OR s.id = :shopId)
        AND (:categoryId IS NULL OR c.id = :categoryId)
        AND (:status IS NULL OR p.status = :status)
        AND (:isDeleted IS NULL OR p.isDeleted = :isDeleted)
        AND (
            :keyword IS NULL OR
            LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(p.productCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(s.shopName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        ORDER BY
            CASE
                WHEN LOWER(p.productCode) LIKE LOWER(CONCAT(:keyword, '%')) THEN 0
                WHEN LOWER(p.name) LIKE LOWER(CONCAT(:keyword, '%')) THEN 1
                ELSE 2
            END,
            p.createdAt DESC
    """,
            countQuery = """
        SELECT COUNT(p) FROM Product p
        LEFT JOIN p.category c
        LEFT JOIN p.shop s
        WHERE (:shopId IS NULL OR s.id = :shopId)
        AND (:categoryId IS NULL OR c.id = :categoryId)
        AND (:status IS NULL OR p.status = :status)
        AND (:isDeleted IS NULL OR p.isDeleted = :isDeleted)
        AND (
            :keyword IS NULL OR
            LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(p.productCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(s.shopName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
    """)
    Page<Product> adminSearchProducts(
            @Param("shopId") Integer shopId,
            @Param("categoryId") Integer categoryId,
            @Param("status") ProductStatus status,
            @Param("keyword") String keyword,
            @Param("isDeleted") Boolean isDeleted,
            Pageable pageable
    );

    Long countByStatus(ProductStatus status);

    @EntityGraph(attributePaths = {"shop", "images"})
    List<Product> findTop3ByOrderBySoldCountDesc();

    @EntityGraph(attributePaths = {"images", "category", "shop"})
    Optional<Product> findById(Integer id);

    @Query("""
        SELECT new com.ecommerce.backend.dto.responses.ProductAutocompleteResponse(
            p.id,
            p.productCode,
            p.name
        )
        FROM Product p
        LEFT JOIN p.category c
        WHERE p.shop.id = :shopId
          AND p.isDeleted = false
          AND (
              LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(p.productCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
        ORDER BY
          CASE
            WHEN LOWER(p.productCode) LIKE LOWER(CONCAT(:keyword, '%')) THEN 0
            WHEN LOWER(p.name) LIKE LOWER(CONCAT(:keyword, '%')) THEN 1
            ELSE 2
          END,
          p.name ASC
    """)
    Page<ProductAutocompleteResponse> autocompleteProducts(
            @Param("shopId") Integer shopId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
        SELECT new com.ecommerce.backend.dto.responses.ProductAutocompleteResponse(
            p.id,
            p.productCode,
            p.name
        )
        FROM Product p
        LEFT JOIN p.category c
        LEFT JOIN p.shop s
        WHERE (:shopId IS NULL OR p.shop.id = :shopId)
          AND p.isDeleted = false
          AND c.isDeleted = false
          AND (
            LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.productCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(s.shopName) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
        ORDER BY
          CASE
            WHEN LOWER(p.productCode) LIKE LOWER(CONCAT(:keyword, '%')) THEN 0
            WHEN LOWER(p.name) LIKE LOWER(CONCAT(:keyword, '%')) THEN 1
            ELSE 2
          END,
          p.createdAt DESC
    """)
    List<ProductAutocompleteResponse> autocompleteAdminProducts(
            @Param("keyword") String keyword,
            @Param("shopId") Integer shopId,
            Pageable pageable
    );

}