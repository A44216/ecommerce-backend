package com.ecommerce.backend.dto.responses.seller.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class SellerRevenueChartResponse {
    private String date;
    private BigDecimal revenue;
}
