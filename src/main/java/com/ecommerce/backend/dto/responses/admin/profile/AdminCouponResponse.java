package com.ecommerce.backend.dto.responses.admin.profile;

import com.ecommerce.backend.enums.CouponStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class AdminCouponResponse {

    private Integer id;
    private String code;

    private Integer discountPercent;
    private BigDecimal discountAmount;

    private BigDecimal minOrderValue;
    private BigDecimal maxDiscountAmount;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Integer maxUsage;
    private Integer usedCount;

    private CouponStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}