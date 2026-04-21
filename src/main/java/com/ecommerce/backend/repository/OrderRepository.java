package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.enums.OrderStatus;
import com.ecommerce.backend.enums.PaymentMethod;
import com.ecommerce.backend.enums.PaymentStatus;
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

public interface OrderRepository extends JpaRepository<Order, Integer> {

    @EntityGraph(attributePaths = { "user" })
    List<Order> findByUserId(Integer userId);

    @EntityGraph(attributePaths = { "user" })
    Page<Order> findByShopId(Integer shopId, Pageable pageable);

    @EntityGraph(attributePaths = { "user" })
    Page<Order> findByShopIdAndStatus(Integer shopId, OrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = { "items" })
    Optional<Order> findByIdAndShopId(Integer id, Integer shopId);

    @Query("""
                SELECT COUNT(o)
                FROM Order o
                WHERE o.shop.id = :shopId
            """)
    Integer countOrderByShop(@Param("shopId") Integer shopId);

    @Query("""
                SELECT COALESCE(SUM(o.subtotal - o.platformFeeAmount), 0)
                FROM Order o
                WHERE o.shop.id = :shopId
                    AND o.status = com.ecommerce.backend.enums.OrderStatus.COMPLETED
                    AND o.paymentStatus = com.ecommerce.backend.enums.PaymentStatus.PAID
            """)
    BigDecimal sumRevenueByShop(@Param("shopId") Integer shopId);

    // REVENUE REPORT
    @Query(value = """
                SELECT DATE(completed_at) AS date,
                       COALESCE(SUM(COALESCE(subtotal, 0) - COALESCE(platform_fee_amount, 0)), 0) AS revenue
                FROM orders
                WHERE shop_id = :shopId
                    AND status = 'COMPLETED'
                    AND payment_status = 'PAID'
                    AND completed_at >= NOW() - INTERVAL 7 DAY
                GROUP BY DATE(completed_at)
                ORDER BY date
            """, nativeQuery = true)
    List<Object[]> getRevenueLast7Days(@Param("shopId") Integer shopId);

    @Query(value = """
                SELECT DATE_FORMAT(completed_at, '%Y-%m') AS date,
                       COALESCE(SUM(COALESCE(subtotal, 0) - COALESCE(platform_fee_amount, 0)), 0) AS revenue
                FROM orders
                WHERE shop_id = :shopId
                    AND status = 'COMPLETED'
                    AND payment_status = 'PAID'
                GROUP BY DATE_FORMAT(completed_at, '%Y-%m')
                ORDER BY date
            """, nativeQuery = true)
    List<Object[]> getRevenueByMonth(@Param("shopId") Integer shopId);

    @Query(value = """
                SELECT YEAR(completed_at) AS date,
                   COALESCE(SUM(COALESCE(subtotal, 0) - COALESCE(platform_fee_amount, 0)), 0) AS revenue
                FROM orders
                WHERE shop_id = :shopId
                    AND status = 'COMPLETED'
                    AND payment_status = 'PAID'
                GROUP BY YEAR(completed_at)
                ORDER BY date
            """, nativeQuery = true)
    List<Object[]> getRevenueByYear(@Param("shopId") Integer shopId);

    @Query("""
                SELECT o FROM Order o
                LEFT JOIN FETCH o.items i
                LEFT JOIN FETCH i.product
                LEFT JOIN FETCH o.user
                LEFT JOIN FETCH o.shop
                LEFT JOIN FETCH o.coupon
                WHERE o.id = :orderId
            """)
    Optional<Order> findByIdWithItems(@Param("orderId") Integer orderId);

    @Query("""
                SELECT COALESCE(SUM(o.subtotal - o.platformFeeAmount), 0)
                FROM Order o
                WHERE o.shop.id = :shopId
                    AND o.status = com.ecommerce.backend.enums.OrderStatus.COMPLETED
                    AND o.paymentStatus = com.ecommerce.backend.enums.PaymentStatus.PAID
                    AND o.completedAt BETWEEN :startDate AND :endDate
            """)
    BigDecimal sumRevenueByShopAndDate(
            @Param("shopId") Integer shopId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("""
                SELECT COUNT(o)
                FROM Order o
                WHERE o.shop.id = :shopId
                    AND o.status = com.ecommerce.backend.enums.OrderStatus.COMPLETED
                    AND o.paymentStatus = com.ecommerce.backend.enums.PaymentStatus.PAID
                    AND o.completedAt BETWEEN :startDate AND :endDate
            """)
    Integer countOrderByShopAndDate(
            @Param("shopId") Integer shopId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @org.springframework.data.jpa.repository.Modifying
    @Query("""
                UPDATE Order o
                SET o.status = com.ecommerce.backend.enums.OrderStatus.CANCELED
                WHERE o.status = com.ecommerce.backend.enums.OrderStatus.PENDING
                    AND o.paymentMethod = com.ecommerce.backend.enums.PaymentMethod.QR
                    AND o.paymentStatus = com.ecommerce.backend.enums.PaymentStatus.UNPAID
                    AND o.createdAt <= :threshold
            """)
    int cancelUnpaidQROrders(@Param("threshold") LocalDateTime threshold);

    @Query("""
                SELECT COALESCE(SUM(o.platformFeeAmount), 0)
                FROM Order o
                WHERE o.status = com.ecommerce.backend.enums.OrderStatus.COMPLETED
                    AND o.paymentStatus = com.ecommerce.backend.enums.PaymentStatus.PAID
                    AND o.completedAt BETWEEN :startDate AND :endDate
            """)
    BigDecimal sumPlatformRevenueByDate(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query(value = """
                SELECT DATE(completed_at) AS date,
                       COALESCE(SUM(platform_fee_amount), 0) AS revenue
                FROM orders
                WHERE status = 'COMPLETED'
                    AND payment_status = 'PAID'
                    AND completed_at >= NOW() - INTERVAL 7 DAY
                GROUP BY DATE(completed_at)
                ORDER BY date
            """, nativeQuery = true)
    List<Object[]> getPlatformRevenueLast7Days();

    @Query(value = """
                SELECT DATE_FORMAT(completed_at, '%Y-%m') AS date,
                       COALESCE(SUM(platform_fee_amount), 0) AS revenue
                FROM orders
                WHERE status = 'COMPLETED'
                    AND payment_status = 'PAID'
                GROUP BY DATE_FORMAT(completed_at, '%Y-%m')
                ORDER BY date
            """, nativeQuery = true)
    List<Object[]> getPlatformRevenueByMonth();

    @Query(value = """
                SELECT YEAR(completed_at) AS date,
                   COALESCE(SUM(platform_fee_amount), 0) AS revenue
                FROM orders
                WHERE status = 'COMPLETED'
                    AND payment_status = 'PAID'
                GROUP BY YEAR(completed_at)
                ORDER BY date
            """, nativeQuery = true)
    List<Object[]> getPlatformRevenueByYear();

    @Query(value = """
                SELECT o FROM Order o
                LEFT JOIN FETCH o.shop s
                LEFT JOIN FETCH o.user u
                WHERE (:status IS NULL OR o.status = :status)
                AND (:paymentMethod IS NULL OR o.paymentMethod = :paymentMethod)
                AND (:paymentStatus IS NULL OR o.paymentStatus = :paymentStatus)
            """, countQuery = """
                SELECT COUNT(o) FROM Order o
                LEFT JOIN o.shop s
                LEFT JOIN o.user u
                WHERE (:status IS NULL OR o.status = :status)
                AND (:paymentMethod IS NULL OR o.paymentMethod = :paymentMethod)
                AND (:paymentStatus IS NULL OR o.paymentStatus = :paymentStatus)
            """)
    Page<Order> adminSearchOrders(
            @Param("status") OrderStatus status,
            @Param("paymentMethod") PaymentMethod paymentMethod,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(o.subtotal - o.platformFeeAmount), 0)
            FROM Order o
            WHERE o.status = com.ecommerce.backend.enums.OrderStatus.COMPLETED
                AND o.paymentStatus = com.ecommerce.backend.enums.PaymentStatus.PAID
                AND o.completedAt BETWEEN :startDate AND :endDate
        """)
    BigDecimal sumGMVByDate(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    Integer countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("""
            SELECT o.status, COUNT(o)
            FROM Order o
            WHERE o.createdAt BETWEEN :startDate AND :endDate
            GROUP BY o.status
        """)
    List<Object[]> countOrdersByStatusAndDate(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query(value = """
            SELECT c.name AS categoryName, COALESCE(SUM(oi.price * oi.quantity), 0) AS totalSales
            FROM orders o
            JOIN order_items oi ON o.id = oi.order_id
            JOIN products p ON oi.product_id = p.id
            JOIN categories c ON p.category_id = c.id
            WHERE o.status = 'COMPLETED' AND o.payment_status = 'PAID'
            AND o.completed_at BETWEEN :startDate AND :endDate
            GROUP BY c.id, c.name
            ORDER BY totalSales DESC
        """, nativeQuery = true)
    List<Object[]> getCategorySalesByDate(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("""
            SELECT new com.ecommerce.backend.dto.responses.admin.dashboard.AdminTopShopResponse(
                o.shop.id,
                o.shop.shopName,
                o.shop.avatar,
                SUM(o.subtotal - o.platformFeeAmount),
                COUNT(o.id)
            )
            FROM Order o
            WHERE o.status = com.ecommerce.backend.enums.OrderStatus.COMPLETED
                AND o.paymentStatus = com.ecommerce.backend.enums.PaymentStatus.PAID
                AND o.completedAt BETWEEN :startDate AND :endDate
            GROUP BY o.shop.id, o.shop.shopName, o.shop.avatar
            ORDER BY SUM(o.subtotal - o.platformFeeAmount) DESC
        """)
    List<com.ecommerce.backend.dto.responses.admin.dashboard.AdminTopShopResponse> adminFindTopShopsByRevenueByDate(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}