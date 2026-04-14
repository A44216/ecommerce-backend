package com.ecommerce.backend.dto.responses.seller.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SellerTopSellingProductResponse {

    private Integer productId;
    private String name;
    private Integer soldQuantity;
    private BigDecimal revenue;
    private String image;

    private BigDecimal price;

    public SellerTopSellingProductResponse(
            Integer productId,
            String name,
            Long soldQuantity,
            BigDecimal revenue,
            String image,
            BigDecimal price
    ) {
        this.productId = productId;
        this.name = name;
        this.soldQuantity = soldQuantity != null ? soldQuantity.intValue() : 0;
        this.revenue = revenue;
        this.image = image;
        this.price = price;
    }
}
