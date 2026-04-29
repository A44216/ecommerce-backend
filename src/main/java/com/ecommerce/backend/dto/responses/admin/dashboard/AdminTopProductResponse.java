package com.ecommerce.backend.dto.responses.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminTopProductResponse {
    private Integer id;
    private String productCode;
    private String name;
    private String image;
    private String shopName;
    private Integer soldCount;
    private BigDecimal revenue;
    private BigDecimal price;

    public AdminTopProductResponse(
            Integer id,
            String productCode,
            String name,
            String image,
            String shopName,
            Long soldCount,
            BigDecimal revenue,
            BigDecimal price
    ) {
        this.id = id;
        this.productCode = productCode;
        this.name = name;
        this.image = image;
        this.shopName = shopName;
        this.soldCount = soldCount != null ? soldCount.intValue() : 0;
        this.revenue = revenue;
        this.price = price;
    }

}
