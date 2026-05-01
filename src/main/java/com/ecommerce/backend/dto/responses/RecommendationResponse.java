package com.ecommerce.backend.dto.responses;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class RecommendationResponse {

    private Integer productId;
    private String productName;
    private String imageUrl;
    private BigDecimal price;

    private BigDecimal score;

    private BigDecimal soldScore;
    private BigDecimal ratingScore;
    private BigDecimal priceScore;

    private String type;
    private String reason;
}