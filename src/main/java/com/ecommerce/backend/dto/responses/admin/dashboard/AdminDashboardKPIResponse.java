package com.ecommerce.backend.dto.responses.admin.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AdminDashboardKPIResponse {
    private Long totalUsers;
    private Long totalShops;
    private BigDecimal totalPlatformRevenue;
    private Long pendingComplaints;
}
