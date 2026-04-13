package com.ecommerce.backend.dto.responses.seller.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class SellerDashboardResponse {

    private BigDecimal revenue;
    private Integer orders;
    private Integer sold;

    private List<SellerTopSellingProductResponse> topProductsByRevenue;
    private List<SellerTopSellingProductResponse> topProductsBySold;

}
