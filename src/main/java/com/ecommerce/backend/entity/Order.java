package com.ecommerce.backend.entity;

import com.ecommerce.backend.enums.OrderStatus;
import com.ecommerce.backend.enums.PaymentMethod;
import com.ecommerce.backend.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        name = "orders",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_orders_order_code", columnNames = "order_code")
        },
        indexes = {
                @Index(name = "idx_orders_status", columnList = "status"),
                @Index(name = "idx_orders_user_created", columnList = "user_id, created_at"),
                @Index(name = "idx_orders_address", columnList = "address_id"),
                @Index(name = "idx_orders_coupon", columnList = "coupon_id"),
                @Index(name = "idx_orders_shop_status", columnList = "shop_id, status"),
                @Index(name = "idx_orders_shop_status_payment", columnList = "shop_id, status, payment_status"),
                @Index(name = "idx_orders_completed_at", columnList = "completed_at"),
                @Index(name = "idx_orders_shop_status_payment_date", columnList = "shop_id, status, payment_status, completed_at"),
                @Index(name = "idx_orders_cron_cancel", columnList = "status, payment_method, payment_status, created_at"),
                @Index(name = "idx_orders_shop_order_code", columnList = "shop_id, order_code")
        }
)
@SuppressWarnings("deprecation")
@Check(name = "chk_orders_discount_amount", constraints = "discount_amount >= 0")
@Check(name = "chk_orders_platform_fee_amount", constraints = "platform_fee_amount >= 0")
@Check(name = "chk_orders_platform_fee_rate", constraints = "platform_fee_rate between 0 and 100")
@Check(name = "chk_orders_subtotal", constraints = "subtotal >= 0")
@Check(name = "chk_orders_total_price", constraints = "total_price >= 0")
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "order_code", nullable = false, unique = true, length = 30)
    private String orderCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    @ColumnDefault("'PENDING'")
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "total_price", nullable = false, precision = 18, scale = 2)
    @ColumnDefault("0.00")
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @Column(name = "subtotal", nullable = false, precision = 18, scale = 2)
    @ColumnDefault("0.00")
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "platform_fee_rate", nullable = false, precision = 5, scale = 2)
    @ColumnDefault("0.00")
    private BigDecimal platformFeeRate = BigDecimal.ZERO;

    @Column(name = "platform_fee_amount", nullable = false, precision = 18, scale = 2)
    @ColumnDefault("0.00")
    private BigDecimal platformFeeAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 18, scale = 2)
    @ColumnDefault("0.00")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    @ColumnDefault("'UNPAID'")
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Column(name = "shipping_name", nullable = false, length = 100)
    private String shippingName;

    @Column(name = "shipping_phone", nullable = false, length = 20)
    private String shippingPhone;

    @Column(name = "shipping_address", nullable = false, length = 255)
    private String shippingAddress;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false, columnDefinition = "TIMESTAMP")
    @ColumnDefault("CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "completed_at", columnDefinition = "TIMESTAMP")
    @ColumnDefault("NULL")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;

}