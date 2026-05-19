package com.ecommerce.backend.entity;

import com.ecommerce.backend.enums.CouponStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "coupons",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_coupons_code", columnNames = "code")
        },
        indexes = {
                @Index(name = "idx_coupons_status_end_date", columnList = "status, end_date")

        }
)
@Check(name = "chk_coupons_discount_percent", constraints = "discount_percent between 0 and 100")
@Check(name = "chk_coupons_discount_type", constraints = "((discount_percent is not null and discount_amount is null) or (discount_percent is null and discount_amount is not null))")
@Check(name = "chk_coupons_max_discount", constraints = "(max_discount_amount >= 0 or max_discount_amount is null)")
@Check(name = "chk_coupons_min_order_value", constraints = "(min_order_value >= 0 or min_order_value is null)")
@Getter
@Setter
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(name = "discount_percent")
    private Integer discountPercent;

    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "min_order_value", precision = 18, scale = 2)
    private BigDecimal minOrderValue;

    @Column(name = "max_discount_amount", precision = 18, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "max_usage")
    private Integer maxUsage;

    @Column(name = "used_count", nullable = false)
    @ColumnDefault("0")
    private Integer usedCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'ACTIVE'")
    private CouponStatus status = CouponStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false, columnDefinition = "TIMESTAMP")
    @ColumnDefault("CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
    @ColumnDefault("CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "TINYINT(1)")
    @ColumnDefault("0")
    private Boolean isDeleted = false;

}