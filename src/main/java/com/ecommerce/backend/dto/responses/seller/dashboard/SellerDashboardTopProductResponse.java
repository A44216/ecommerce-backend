package com.ecommerce.backend.dto.responses.seller.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class SellerDashboardTopProductResponse {

    private List<SellerTopSellingProductResponse> topByRevenue;
    private List<SellerTopSellingProductResponse> topBySold;
}
