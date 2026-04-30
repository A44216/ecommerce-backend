package com.ecommerce.backend.dto.responses;

import com.ecommerce.backend.enums.ProductStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Integer id;
    private String name;
    private BigDecimal price;
    private int stock;
    private String description;

    private String categoryName;
    private Integer shopId;
    private String shopName;

    private BigDecimal ratingAvg;
    private int ratingCount;
    private int soldCount;

    private ProductStatus status;

    private List<ProductImageResponse> images;

}