package com.ecommerce.backend.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@Builder
public class RecommendationResponse {

    private Integer productId;
    private String productName;
    private String imageUrl;
    private BigDecimal price;
    private BigDecimal score;
}