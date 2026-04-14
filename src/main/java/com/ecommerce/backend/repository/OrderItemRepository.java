package com.ecommerce.backend.repository;

import com.ecommerce.backend.dto.responses.seller.dashboard.SellerTopSellingProductResponse;
import com.ecommerce.backend.entity.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

    List<OrderItem> findByOrderId(Integer orderId);

    // TOTAL SOLD QUANTITY
    @Query("""
        SELECT COALESCE(SUM(oi.quantity), 0)
        FROM OrderItem oi
        WHERE oi.order.shop.id = :shopId
    """)
    Integer sumQuantityByShopId(@Param("shopId") Integer shopId);

    // TOP PRODUCTS BY REVENUE (LIMIT được truyền từ service)
    @Query("""
        SELECT new com.ecommerce.backend.dto.responses.seller.dashboard.SellerTopSellingProductResponse(
            oi.product.id,
            oi.product.name,
            SUM(oi.quantity),
            SUM(oi.price * oi.quantity),
            MIN(pi.imageUrl),
            MIN(oi.price)
        )
        FROM OrderItem oi
        LEFT JOIN oi.product.images pi
        WHERE oi.order.shop.id = :shopId
          AND oi.order.status = com.ecommerce.backend.enums.OrderStatus.COMPLETED
          AND oi.order.paymentStatus = com.ecommerce.backend.enums.PaymentStatus.PAID
        GROUP BY oi.product.id, oi.product.name
        ORDER BY SUM(oi.price * oi.quantity) DESC
    """)
    List<SellerTopSellingProductResponse> findTopByRevenue(
            @Param("shopId") Integer shopId,
            Pageable pageable
    );

    // TOP PRODUCTS BY SOLD (nếu cần sau này)
    @Query("""
        SELECT new com.ecommerce.backend.dto.responses.seller.dashboard.SellerTopSellingProductResponse(
            oi.product.id,
            oi.product.name,
            SUM(oi.quantity),
            SUM(oi.price * oi.quantity),
            MIN(pi.imageUrl),
            MIN(oi.price)
        )
        FROM OrderItem oi
        LEFT JOIN oi.product.images pi
        WHERE oi.order.shop.id = :shopId
          AND oi.order.status = com.ecommerce.backend.enums.OrderStatus.COMPLETED
          AND oi.order.paymentStatus = com.ecommerce.backend.enums.PaymentStatus.PAID
        GROUP BY oi.product.id, oi.product.name
        ORDER BY SUM(oi.quantity) DESC
    """)
    List<SellerTopSellingProductResponse> findTopBySold(
            @Param("shopId") Integer shopId,
            Pageable pageable
    );

}