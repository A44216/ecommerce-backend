package com.ecommerce.backend.dto.responses.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCategorySalesChartResponse {
    private String categoryName;
    private BigDecimal totalSales;
}
