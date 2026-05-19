package com.ecommerce.backend.entity;

import com.ecommerce.backend.enums.ShopStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "shops",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_shops_user", columnNames = "user_id")
        }
)
@Getter
@Setter
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "shop_name", nullable = false, length = 100)
    private String shopName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    @ColumnDefault("'PENDING'")
    private ShopStatus status = ShopStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false, columnDefinition = "TIMESTAMP")
    @ColumnDefault("CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "address")
    private String address;

    @Column(name = "rating_avg", precision = 3, scale = 2, nullable = false)
    @ColumnDefault("0.00")
    private BigDecimal ratingAvg = BigDecimal.ZERO;

    @Column(name = "rating_count", nullable = false)
    @ColumnDefault("0")
    private Integer ratingCount = 0;

    @Column(name = "total_orders", nullable = false)
    @ColumnDefault("0")
    private Integer totalOrders = 0;

    @Column(name = "total_revenue", precision = 18, scale = 2, nullable = false)
    @ColumnDefault("0.00")
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "is_ai_reply_enabled", nullable = false, columnDefinition = "TINYINT(1)")
    @ColumnDefault("0")
    private Boolean isAiReplyEnabled = false;

}