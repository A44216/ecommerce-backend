package com.ecommerce.backend.dto.responses.seller.dashboard;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SellerTopSellingProductResponse {

    private Integer productId;
    private String productCode;
    private String name;
    private Long soldQuantity;
    private BigDecimal revenue;
    private String image;

    private BigDecimal price;

}
