package com.ecommerce.backend.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@Builder
public class OrderItemResponse {

    private Integer id;
    private Integer productId;
    private String productName;
    private String productImage;
    private BigDecimal price;
    private Integer quantity;
    private Boolean isReviewed;
    private Integer reviewId;

}