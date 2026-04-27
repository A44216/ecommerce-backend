package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Shop;
import com.ecommerce.backend.enums.ShopStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Integer> {

    Optional<Shop> findByUserId(Integer userId);

    @Query("SELECT s FROM Shop s JOIN FETCH s.user WHERE s.user.id = :userId")
    Optional<Shop> findByUserIdFetchUser(@Param("userId") Integer userId);

    @Query("SELECT s FROM Shop s WHERE s.user.username = :username")
    Optional<Shop> findByUsername(@Param("username") String username);

    Long countByStatus(ShopStatus status);

    @Query("SELECT s FROM Shop s WHERE " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(s.shopName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "s.phone LIKE CONCAT('%', :keyword, '%'))")
    Page<Shop> searchShops(
            @Param("status") ShopStatus status,
            @Param("keyword") String keyword,
            Pageable pageable);

    List<Shop> findTop3ByOrderByTotalRevenueDesc();

//    @Query("""
//    SELECT\s
//    CASE\s
//        WHEN LOWER(s.shopName) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN s.shopName
//        WHEN LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN s.email
//        WHEN s.phone LIKE CONCAT('%', :keyword, '%') THEN s.phone
//        ELSE s.shopName
//    END
//    FROM Shop s
//    WHERE (:keyword IS NULL OR :keyword = '' OR
//        LOWER(s.shopName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
//        LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
//        s.phone LIKE CONCAT('%', :keyword, '%')
//    )
//    ORDER BY
//    CASE
//        WHEN LOWER(s.shopName) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN 1
//        WHEN LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN 2
//        WHEN s.phone LIKE CONCAT('%', :keyword, '%') THEN 3
//        ELSE 4
//    END,
//    s.shopName ASC
//   \s""")
//    List<String> autocompleteShops(@Param("keyword") String keyword);

    @Query(value = """
        SELECT value FROM (
             (
                 SELECT s.shop_name AS value, 1 AS priority
                 FROM shops s
                 WHERE LOWER(s.shop_name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 ORDER BY s.created_at DESC
                 LIMIT 5
             )
             UNION ALL
             (
                 SELECT s.email AS value, 2 AS priority
                 FROM shops s
                 WHERE LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 ORDER BY s.created_at DESC
                 LIMIT 5
             )
             UNION ALL
             (
                 SELECT s.phone AS value, 3 AS priority
                 FROM shops s
                 WHERE s.phone LIKE CONCAT('%', :keyword, '%')
                 ORDER BY s.created_at DESC
                 LIMIT 5
             )
        ) t
        ORDER BY priority
        LIMIT 5
    """, nativeQuery = true)
    List<String> autocompleteShops(@Param("keyword") String keyword);

}