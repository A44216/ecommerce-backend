package com.ecommerce.backend.repository;

import com.ecommerce.backend.dto.responses.admin.dashboard.AdminTopProductResponse;
import com.ecommerce.backend.dto.responses.seller.dashboard.SellerTopSellingProductResponse;
import com.ecommerce.backend.entity.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

    List<OrderItem> findByOrderId(Integer orderId);

    // TOTAL SOLD
    @Query("""
        SELECT COALESCE(SUM(oi.quantity), 0)
        FROM OrderItem oi
        WHERE oi.order.shop.id = :shopId
            AND oi.order.status = com.ecommerce.backend.enums.OrderStatus.COMPLETED
            AND oi.order.paymentStatus = com.ecommerce.backend.enums.PaymentStatus.PAID
    """)
    Integer sumQuantityByShop(@Param("shopId") Integer shopId);

    // TOP PRODUCTS BY REVENUE (LIMIT được truyền từ service)
    @Query("""
        SELECT new com.ecommerce.backend.dto.responses.seller.dashboard.SellerTopSellingProductResponse(
            oi.product.id,
            oi.product.productCode,
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
        GROUP BY oi.product.id, oi.product.productCode, oi.product.name
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
            oi.product.productCode,
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
        GROUP BY oi.product.id, oi.product.productCode, oi.product.name
        ORDER BY SUM(oi.quantity) DESC
    """)
    List<SellerTopSellingProductResponse> findTopBySold(
            @Param("shopId") Integer shopId,
            Pageable pageable
    );

    @Query("""
        SELECT COALESCE(SUM(oi.quantity), 0)
        FROM OrderItem oi
        WHERE oi.order.shop.id = :shopId
            AND oi.order.status = com.ecommerce.backend.enums.OrderStatus.COMPLETED
            AND oi.order.paymentStatus = com.ecommerce.backend.enums.PaymentStatus.PAID
            AND oi.order.completedAt BETWEEN :startDate AND :endDate
    """)
    Integer sumQuantityByShopAndDate(
            @Param("shopId") Integer shopId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
        SELECT new com.ecommerce.backend.dto.responses.seller.dashboard.SellerTopSellingProductResponse(
            oi.product.id,
            oi.product.productCode,
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
            AND oi.order.completedAt BETWEEN :startDate AND :endDate
        GROUP BY oi.product.id, oi.product.productCode, oi.product.name
        ORDER BY SUM(oi.price * oi.quantity) DESC
    """)
    List<SellerTopSellingProductResponse> findTopByRevenueByDate(
            @Param("shopId") Integer shopId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("""
        SELECT new com.ecommerce.backend.dto.responses.seller.dashboard.SellerTopSellingProductResponse(
            oi.product.id,
            oi.product.productCode,
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
            AND oi.order.completedAt BETWEEN :startDate AND :endDate
        GROUP BY oi.product.id, oi.product.productCode, oi.product.name
        ORDER BY SUM(oi.quantity) DESC
    """)
    List<SellerTopSellingProductResponse> findTopBySoldByDate(
            @Param("shopId") Integer shopId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("""
        SELECT new com.ecommerce.backend.dto.responses.admin.dashboard.AdminTopProductResponse(
            oi.product.id,
            oi.product.productCode,
            oi.product.name,
            MIN(pi.imageUrl),
            oi.product.shop.shopName,
            CAST(SUM(oi.quantity) as integer),
            SUM(oi.price * oi.quantity),
            MIN(oi.price)
        )
        FROM OrderItem oi
        LEFT JOIN oi.product.images pi
        WHERE oi.order.status = com.ecommerce.backend.enums.OrderStatus.COMPLETED
            AND oi.order.paymentStatus = com.ecommerce.backend.enums.PaymentStatus.PAID
            AND oi.order.completedAt BETWEEN :startDate AND :endDate
        GROUP BY oi.product.id, oi.product.productCode, oi.product.name, oi.product.shop.shopName
        ORDER BY SUM(oi.price * oi.quantity) DESC
    """)
    List<AdminTopProductResponse> adminFindTopByRevenueByDate(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("""
        SELECT new com.ecommerce.backend.dto.responses.admin.dashboard.AdminTopProductResponse(
            oi.product.id,
            oi.product.productCode,
            oi.product.name,
            MIN(pi.imageUrl),
            oi.product.shop.shopName,
            CAST(SUM(oi.quantity) as integer),
            SUM(oi.price * oi.quantity),
            MIN(oi.price)
        )
        FROM OrderItem oi
        LEFT JOIN oi.product.images pi
        WHERE oi.order.status = com.ecommerce.backend.enums.OrderStatus.COMPLETED
            AND oi.order.paymentStatus = com.ecommerce.backend.enums.PaymentStatus.PAID
            AND oi.order.completedAt BETWEEN :startDate AND :endDate
        GROUP BY oi.product.id, oi.product.productCode, oi.product.name, oi.product.shop.shopName
        ORDER BY SUM(oi.quantity) DESC
    """)
    List<AdminTopProductResponse> adminFindTopBySoldByDate(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

}