package com.ecommerce.backend.dto.responses.admin.product;

import com.ecommerce.backend.enums.ProductStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminProductResponse {
    private Integer id;
    private String productCode;
    private Integer shopId;
    private String shopName;
    private Integer categoryId;
    private String categoryName;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private ProductStatus status;
    private Integer soldCount;
    private LocalDateTime createdAt;

    private String image;

}
