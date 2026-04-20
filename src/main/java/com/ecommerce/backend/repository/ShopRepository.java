package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Shop;
import com.ecommerce.backend.enums.ShopStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
           "(:keyword IS NULL OR :keyword = '' OR LOWER(s.shopName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    org.springframework.data.domain.Page<Shop> searchShops(
            @Param("status") ShopStatus status,
            @Param("keyword") String keyword,
            org.springframework.data.domain.Pageable pageable);
}