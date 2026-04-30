package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends org.springframework.data.jpa.repository.JpaRepository<Review, Integer> {

    @EntityGraph(attributePaths = {"user", "product"})
    @Query("""
        SELECT r FROM Review r
        WHERE r.product.shop.id = :shopId
        AND r.product.id = :productId
        AND (
            :isReplied IS NULL OR
            (:isReplied = TRUE AND r.sellerReply IS NOT NULL) OR
            (:isReplied = FALSE AND r.sellerReply IS NULL)
        )
    """)
    Page<Review> findByShopFilter(
            @Param("shopId") Integer shopId,
            @Param("productId") Integer productId,
            @Param("isReplied") Boolean isReplied,
            Pageable pageable
    );

    Page<Review> findByProductId(Integer productId, Pageable pageable);

    Page<Review> findByUserId(Integer userId, Pageable pageable);

    List<Review> findByProductId(Integer productId);

    List<Review> findByUserId(Integer userId);
}