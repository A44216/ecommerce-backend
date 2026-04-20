package com.ecommerce.backend.dto.responses.admin.product;

import com.ecommerce.backend.enums.ProductStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminProductDetailResponse {
    private Integer id;
    private Integer shopId;
    private String shopName;
    private Integer categoryId;
    private String categoryName;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private String description;
    private ProductStatus status;
    private BigDecimal ratingAvg;
    private Integer ratingCount;
    private Integer soldCount;
    private LocalDateTime createdAt;
    private List<String> images;
}
