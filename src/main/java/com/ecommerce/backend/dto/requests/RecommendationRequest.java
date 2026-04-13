package com.ecommerce.backend.dto.requests;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RecommendationRequest {

    @NotNull(message = "User ID is required")
    private Integer userId;

    @NotNull(message = "Product ID is required")
    private Integer productId;

    @NotNull(message = "Score is required")
    @DecimalMin(value = "0.0", message = "Score must be at least 0.0")
    @DecimalMax(value = "5.0", message = "Score cannot exceed 5.0")
    private BigDecimal score;

}