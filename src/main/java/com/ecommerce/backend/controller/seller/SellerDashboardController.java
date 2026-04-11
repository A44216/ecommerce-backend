package com.ecommerce.backend.controller.seller;

import com.ecommerce.backend.dto.responses.seller.dashboard.SellerDashboardResponse;
import com.ecommerce.backend.dto.responses.seller.dashboard.SellerRevenueChartResponse;
import com.ecommerce.backend.enums.ChartType;
import com.ecommerce.backend.service.seller.SellerDashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/seller")
public class SellerDashboardController {

    private final SellerDashboardService dashboardService;

    public SellerDashboardController(SellerDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public SellerDashboardResponse getDashboard(Authentication authentication) {
        return dashboardService.getDashboard(authentication);
    }

    @GetMapping("/dashboard/revenue-chart")
    public List<SellerRevenueChartResponse> getChart(
            Authentication authentication,
            @RequestParam(defaultValue = "DAY") ChartType type
    ) {
        return dashboardService.getRevenueChart(authentication, type);
    }
}
