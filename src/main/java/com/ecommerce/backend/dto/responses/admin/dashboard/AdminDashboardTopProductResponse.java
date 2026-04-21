package com.ecommerce.backend.dto.responses.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardTopProductResponse {
    private List<AdminTopProductResponse> topByRevenue;
    private List<AdminTopProductResponse> topBySold;
}
