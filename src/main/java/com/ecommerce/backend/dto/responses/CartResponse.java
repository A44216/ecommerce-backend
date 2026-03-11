package com.ecommerce.backend.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class CartResponse {

    private Integer id;

    private LocalDateTime createdAt;

    private BigDecimal totalPrice;

    private List<CartItemResponse> items;

}