package com.ecommerce.backend.controller.seller;

import com.ecommerce.backend.dto.responses.seller.dashboard.SellerDashboardKPIResponse;
import com.ecommerce.backend.dto.responses.seller.dashboard.SellerDashboardTopProductResponse;
import com.ecommerce.backend.dto.responses.seller.dashboard.SellerRevenueChartResponse;
import com.ecommerce.backend.enums.ChartType;
import com.ecommerce.backend.service.seller.SellerDashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@PreAuthorize("hasRole('SELLER')")
@RestController
@RequestMapping("/api/seller")
public class SellerDashboardController {

    private final SellerDashboardService dashboardService;

    public SellerDashboardController(SellerDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // KPI
    @GetMapping("/dashboard/kpi")
    public SellerDashboardKPIResponse getKPI(
            Authentication authentication,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        return dashboardService.getKPI(authentication, startDate, endDate);
    }

    // TOP PRODUCT
    @GetMapping("/dashboard/top-products")
    public SellerDashboardTopProductResponse getTopProducts(
            Authentication authentication,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        return dashboardService.getTopProducts(authentication, startDate, endDate);
    }

    // CHART
    @GetMapping("/dashboard/revenue-chart")
    public List<SellerRevenueChartResponse> getChart(
            Authentication authentication,
            @RequestParam(defaultValue = "DAY") ChartType type
    ) {
        return dashboardService.getRevenueChart(authentication, type);
    }
}
