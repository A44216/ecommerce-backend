package com.ecommerce.backend.dto.responses.seller.product;

import com.ecommerce.backend.dto.responses.ProductImageResponse;
import com.ecommerce.backend.enums.ProductStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class SellerProductResponse {

    private Integer id;
    private String name;
    private BigDecimal price;
    private int stock;
    private String description;

    private String categoryName;
    private String shopName;

    private BigDecimal ratingAvg;
    private int ratingCount;
    private int soldCount;

    private ProductStatus status;
    private Boolean isDeleted;

    private List<ProductImageResponse> images;

}