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

    @Query(value = "SELECT * FROM shops WHERE user_id = :userId", nativeQuery = true)
    Optional<Shop> findByUserId(@Param("userId") Integer userId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(value = "DELETE FROM shops WHERE user_id = :userId", nativeQuery = true)
    void deleteByUserIdNative(@Param("userId") Integer userId);

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


    @Query(value = """
        SELECT id, value FROM (
             (
                 SELECT s.id AS id, s.shop_name AS value, 1 AS priority
                 FROM shops s
                 WHERE LOWER(s.shop_name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 ORDER BY s.created_at DESC
                 LIMIT 5
             )
             UNION ALL
             (
                 SELECT s.id AS id, s.email AS value, 2 AS priority
                 FROM shops s
                 WHERE LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 ORDER BY s.created_at DESC
                 LIMIT 5
             )
             UNION ALL
             (
                 SELECT s.id AS id, s.phone AS value, 3 AS priority
                 FROM shops s
                 WHERE s.phone LIKE CONCAT('%', :keyword, '%')
                 ORDER BY s.created_at DESC
                 LIMIT 5
             )
        ) t
        GROUP BY id, value
        ORDER BY MIN(priority)
        LIMIT 5
    """, nativeQuery = true)
    List<Object[]> autocompleteShops(@Param("keyword") String keyword);

}