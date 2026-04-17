package com.ecommerce.backend.dto.requests.admin.profile;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class AdminCouponRequest {

    @NotBlank(message = "Coupon code is required")
    @Size(max = 50, message = "Coupon code must be less than 50 characters")
    private String code;

    @Min(value = 0, message = "Discount percent cannot be negative")
    @Max(value = 100, message = "Discount percent cannot exceed 100")
    private Integer discountPercent;

    @DecimalMin(value = "0.0", message = "Discount amount cannot be negative")
    private BigDecimal discountAmount;

    @NotNull(message = "Minimum order value is required")
    @DecimalMin(value = "0.0", message = "Minimum order value cannot be negative")
    private BigDecimal minOrderValue;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Min(value = 1, message = "Max usage must be at least 1")
    private Integer maxUsage;

    private BigDecimal maxDiscountAmount;

}