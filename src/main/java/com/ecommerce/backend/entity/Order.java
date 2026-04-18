package com.ecommerce.backend.entity;

import com.ecommerce.backend.enums.OrderStatus;
import com.ecommerce.backend.enums.PaymentMethod;
import com.ecommerce.backend.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_orders_status", columnList = "status"),
                @Index(name = "idx_orders_user_created", columnList = "user_id, created_at"),
                @Index(name = "idx_orders_address", columnList = "address_id"),
                @Index(name = "idx_orders_coupon", columnList = "coupon_id"),
                @Index(name = "idx_orders_shop_status", columnList = "shop_id, status"),
                @Index(name = "idx_orders_shop_status_payment", columnList = "shop_id, status, payment_status"),
                @Index(name = "idx_orders_completed_at", columnList = "completed_at"),
                @Index(name = "idx_orders_shop_status_payment_date", columnList = "shop_id, status, payment_status, completed_at")
        }
)
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "total_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @Column(name = "subtotal", nullable = false, precision = 18, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "platform_fee_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal platformFeeRate = BigDecimal.ZERO;

    @Column(name = "platform_fee_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal platformFeeAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Column(name = "shipping_name", length = 100)
    private String shippingName;

    @Column(name = "shipping_phone", length = 20)
    private String shippingPhone;

    @Column(name = "shipping_address", length = 255)
    private String shippingAddress;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;

}