package com.ecommerce.backend.repository;

import com.ecommerce.backend.enums.ShopStatus;

import com.ecommerce.backend.dto.responses.ProductAutocompleteResponse;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @EntityGraph(attributePaths = {"images", "category", "shop"})
    Optional<Product> findByIdAndIsDeletedFalse(Integer id);

    Integer countByShopIdAndIsDeletedFalse(Integer shopId);

    List<Product> findTop5ByShopIdAndIsDeletedFalseAndStatusOrderByCreatedAtDesc(Integer shopId, ProductStatus status);

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
        AND (:inStock IS NULL OR (:inStock = true AND p.stock > 0) OR (:inStock = false AND p.stock = 0))
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
            END
    """)
    Page<Product> filterProducts(
            @Param("shopId") Integer shopId,
            @Param("isDeleted") Boolean isDeleted,
            @Param("status") ProductStatus status,
            @Param("inStock") Boolean inStock,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query(value = """
        SELECT p FROM Product p
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
        SELECT DISTINCT new com.ecommerce.backend.dto.responses.ProductAutocompleteResponse(
            p.productCode,
            p.name
        )
        FROM Product p
        LEFT JOIN p.category c
        WHERE p.shop.id = :shopId
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
        SELECT DISTINCT new com.ecommerce.backend.dto.responses.ProductAutocompleteResponse(
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
            WHEN LOWER(p.productCode) = LOWER(:keyword) THEN 0
            WHEN LOWER(p.productCode) LIKE LOWER(CONCAT(:keyword, '%')) THEN 1
            WHEN LOWER(p.name) LIKE LOWER(CONCAT(:keyword, '%')) THEN 2
            ELSE 3
          END,
          p.productCode ASC
    """)
    List<ProductAutocompleteResponse> autocompleteAdminProducts(
            @Param("keyword") String keyword,
            @Param("shopId") Integer shopId,
            Pageable pageable
    );


    List<Product> findByIsDeletedTrue();

    List<Product> findByStatusAndIsDeletedFalseAndShopStatus(ProductStatus status, ShopStatus shopStatus);

    org.springframework.data.domain.Page<Product> findByStatusAndIsDeletedFalseAndShopStatus(ProductStatus status, ShopStatus shopStatus, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<Product> findByNameContainingIgnoreCaseAndStatusAndIsDeletedFalseAndShopStatus(String keyword, ProductStatus status, ShopStatus shopStatus, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<Product> findByCategoryIdAndStatusAndIsDeletedFalseAndShopStatus(Integer categoryId, ProductStatus status, ShopStatus shopStatus, org.springframework.data.domain.Pageable pageable);

    List<Product> findTop10ByNameContainingIgnoreCaseAndStatusAndIsDeletedFalseAndShopStatus(String keyword, ProductStatus status, ShopStatus shopStatus);

    List<Product> findTop10ByStatusAndIsDeletedFalseAndShopStatusOrderBySoldCountDesc(ProductStatus status, ShopStatus shopStatus);

    Optional<Product> findByIdAndStatusAndIsDeletedFalseAndShopStatus(Integer id, ProductStatus status, ShopStatus shopStatus);

    List<Product> findByNameContainingIgnoreCaseAndStatusAndIsDeletedFalseAndShopStatus(String keyword, ProductStatus status, ShopStatus shopStatus);

    List<Product> findByCategoryIdAndStatusAndIsDeletedFalseAndShopStatus(Integer categoryId, ProductStatus status, ShopStatus shopStatus);

    List<Product> findByShopIdAndStatusAndIsDeletedFalseAndShopStatus(Integer shopId, ProductStatus status, ShopStatus shopStatus);

    // Các hàm cho Xai và Fuzzy
    // Query context 1 lần cho tất cả categories (tối ưu: thay 3N query bằng 1 query)
    @Query("""
        SELECT p.category.id,
               COALESCE(MAX(p.soldCount), 0),
               MIN(p.price),
               MAX(p.price)
        FROM Product p
        WHERE p.status = 'APPROVED' AND p.isDeleted = false AND p.shop.status = 'APPROVED'
        GROUP BY p.category.id
    """)
    List<Object[]> findAllCategoryContexts();

    @Query("SELECT MAX(p.soldCount) FROM Product p WHERE p.category.id = :categoryId")
    Integer findMaxSoldCountByCategoryId(@Param("categoryId") Integer categoryId);

    @Query("SELECT MIN(p.price) FROM Product p WHERE p.category.id = :categoryId")
    BigDecimal findMinPriceByCategoryId(@Param("categoryId") Integer categoryId);

    @Query("SELECT MAX(p.price) FROM Product p WHERE p.category.id = :categoryId")
    BigDecimal findMaxPriceByCategoryId(@Param("categoryId") Integer categoryId);

    // Batch query: đếm sales cho nhiều sản phẩm trong 1 query (tối ưu: thay N query bằng 1 query)
    @Query("""
        SELECT oi.product.id, COALESCE(SUM(oi.quantity), 0)
        FROM OrderItem oi
        JOIN oi.order o
        WHERE oi.product.id IN :productIds
        AND o.status = 'COMPLETED'
        AND o.createdAt >= :startDate
        GROUP BY oi.product.id
    """)
    List<Object[]> countRecentSalesBatch(
            @Param("productIds") List<Integer> productIds,
            @Param("startDate") LocalDateTime startDate
    );

    @Query("""
        SELECT COALESCE(SUM(oi.quantity), 0) 
        FROM OrderItem oi 
        JOIN oi.order o 
        WHERE oi.product.id = :productId 
        AND o.status = 'COMPLETED' 
        AND o.createdAt >= :startDate
    """)
    Integer countRecentSales(@Param("productId") Integer productId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT AVG(p.ratingAvg) FROM Product p WHERE p.shop.id = :shopId AND p.ratingCount > 0 AND p.isDeleted = false")
    BigDecimal getAverageRatingByShopId(@Param("shopId") Integer shopId);

    @Query("SELECT SUM(p.ratingCount) FROM Product p WHERE p.shop.id = :shopId AND p.isDeleted = false")
    Integer getTotalRatingCountByShopId(@Param("shopId") Integer shopId);

}