package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    @EntityGraph(attributePaths = {"user"})
    List<Order> findByUserId(Integer userId);

    @EntityGraph(attributePaths = {"user"})
    Page<Order> findByShopId(Integer shopId, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Page<Order> findByShopIdAndStatus(Integer shopId, OrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"items"})
    Optional<Order> findByIdAndShopId(Integer id, Integer shopId);

    @Query("""
        SELECT COUNT(o)
        FROM Order o
        WHERE o.shop.id = :shopId
    """)
    Integer countByShopId(@Param("shopId") Integer shopId);

    @Query("""
        SELECT COALESCE(SUM(o.totalPrice), 0)
        FROM Order o
        WHERE o.shop.id = :shopId
          AND o.status = com.ecommerce.backend.enums.OrderStatus.COMPLETED
          AND o.paymentStatus = com.ecommerce.backend.enums.PaymentStatus.PAID
    """)
    BigDecimal sumRevenueByShop(@Param("shopId") Integer shopId);

    // ================= REVENUE REPORT =================
    @Query(value = """
        SELECT DATE(created_at) AS date,
               COALESCE(SUM(total_price), 0) AS revenue
        FROM orders
        WHERE shop_id = :shopId
          AND status = 'COMPLETED'
          AND payment_status = 'PAID'
          AND created_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
        GROUP BY DATE(created_at)
        ORDER BY date
    """, nativeQuery = true)
    List<Object[]> getRevenueLast7Days(@Param("shopId") Integer shopId);

    @Query(value = """
        SELECT DATE_FORMAT(created_at, '%Y-%m') AS date,
               COALESCE(SUM(total_price), 0) AS revenue
        FROM orders
        WHERE shop_id = :shopId
          AND status = 'COMPLETED'
          AND payment_status = 'PAID'
        GROUP BY DATE_FORMAT(created_at, '%Y-%m')
        ORDER BY date
    """, nativeQuery = true)
    List<Object[]> getRevenueByMonth(@Param("shopId") Integer shopId);

    @Query(value = """
        SELECT YEAR(created_at) AS date,
               COALESCE(SUM(total_price), 0) AS revenue
        FROM orders
        WHERE shop_id = :shopId
          AND status = 'COMPLETED'
          AND payment_status = 'PAID'
        GROUP BY YEAR(created_at)
        ORDER BY date
    """, nativeQuery = true)
    List<Object[]> getRevenueByYear(@Param("shopId") Integer shopId);

    @Query("""
        SELECT o FROM Order o
        LEFT JOIN FETCH o.items
        LEFT JOIN FETCH o.user
        LEFT JOIN FETCH o.shop
        WHERE o.id = :orderId
    """)
    Optional<Order> findByIdWithItems(@Param("orderId") Integer orderId);
}