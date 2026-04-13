package com.ecommerce.backend.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class CartItemResponse {

    private Integer id;
    private Integer productId;
    private String productName;
    private String image;
    private BigDecimal price;
    private int quantity;

}