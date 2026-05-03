package com.ecommerce.backend.dto.responses;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@Builder
public class RecommendationResponse {
    private Integer id;
    private BigDecimal score;
    private String reason;
    private ProductBaseResponse product;
}