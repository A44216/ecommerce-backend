package com.ecommerce.backend.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class RecommendationResponse {

    private Integer productId;
    private String productName;
    private String imageUrl;
    private BigDecimal price;
    private BigDecimal score;
}