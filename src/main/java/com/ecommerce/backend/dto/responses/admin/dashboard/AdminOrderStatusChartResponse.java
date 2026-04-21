package com.ecommerce.backend.dto.responses.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderStatusChartResponse {
    private String status;
    private Long orderCount;
}
