package com.ecommerce.backend.controller.admin;

import com.ecommerce.backend.dto.responses.admin.dashboard.AdminDashboardKPIResponse;
import com.ecommerce.backend.dto.responses.admin.dashboard.AdminRevenueChartResponse;
import com.ecommerce.backend.enums.ChartType;
import com.ecommerce.backend.enums.DateRange;
import com.ecommerce.backend.service.admin.AdminDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/kpi")
    public ResponseEntity<AdminDashboardKPIResponse> getKPI(
            @RequestParam(defaultValue = "TODAY") DateRange range) {
        return ResponseEntity.ok(adminDashboardService.getKPI(range));
    }

    @GetMapping("/revenue-chart")
    public ResponseEntity<List<AdminRevenueChartResponse>> getRevenueChart(
            @RequestParam(defaultValue = "DAY") ChartType type) {
        return ResponseEntity.ok(adminDashboardService.getRevenueChart(type));
    }

    @GetMapping("/order-status-chart")
    public ResponseEntity<List<com.ecommerce.backend.dto.responses.admin.dashboard.AdminOrderStatusChartResponse>> getOrderStatusChart(
            @RequestParam(defaultValue = "TODAY") DateRange range) {
        return ResponseEntity.ok(adminDashboardService.getOrderStatusChart(range));
    }

    @GetMapping("/category-sales-chart")
    public ResponseEntity<List<com.ecommerce.backend.dto.responses.admin.dashboard.AdminCategorySalesChartResponse>> getCategorySalesChart(
            @RequestParam(defaultValue = "TODAY") DateRange range) {
        return ResponseEntity.ok(adminDashboardService.getCategorySalesChart(range));
    }

    @GetMapping("/top-selling-shops")
    public ResponseEntity<List<com.ecommerce.backend.dto.responses.admin.dashboard.AdminTopShopResponse>> getTopSellingShops(
            @RequestParam(defaultValue = "TODAY") DateRange range) {
        return ResponseEntity.ok(adminDashboardService.getTopSellingShops(range));
    }

    @GetMapping("/top-selling-products")
    public ResponseEntity<com.ecommerce.backend.dto.responses.admin.dashboard.AdminDashboardTopProductResponse> getTopSellingProducts(
            @RequestParam(defaultValue = "TODAY") DateRange range) {
        return ResponseEntity.ok(adminDashboardService.getTopSellingProducts(range));
    }
}
