package com.ecommerce.backend.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductBaseResponse {
    private Integer productId;
    private String productName;
    private String productCode;
    private BigDecimal price;
    private String imageUrl;
}
