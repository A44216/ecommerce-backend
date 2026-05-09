package com.ecommerce.backend.service.admin;

import com.ecommerce.backend.dto.responses.admin.dashboard.AdminDashboardKPIResponse;
import com.ecommerce.backend.dto.responses.admin.dashboard.AdminRevenueChartResponse;
import com.ecommerce.backend.dto.responses.admin.dashboard.AdminOrderStatusChartResponse;
import com.ecommerce.backend.dto.responses.admin.dashboard.AdminCategorySalesChartResponse;
import com.ecommerce.backend.dto.responses.admin.dashboard.AdminTopShopResponse;
import com.ecommerce.backend.dto.responses.admin.dashboard.AdminDashboardTopProductResponse;
import com.ecommerce.backend.enums.ChartType;
import com.ecommerce.backend.enums.ComplaintStatus;
import com.ecommerce.backend.enums.DateRange;
import com.ecommerce.backend.enums.ShopStatus;
import com.ecommerce.backend.repository.ComplaintRepository;
import com.ecommerce.backend.repository.OrderItemRepository;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.ShopRepository;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.repository.CouponRepository;
import com.ecommerce.backend.util.DateRangeResult;
import com.ecommerce.backend.util.DateRangeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final OrderRepository orderRepository;
    private final ComplaintRepository complaintRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final OrderItemRepository orderItemRepository;

    public AdminDashboardKPIResponse getKPI(DateRange range) {

        DateRangeResult dateRange = DateRangeUtil.getRange(range);

        LocalDateTime startDate = dateRange.start();
        LocalDateTime endDate = dateRange.end();

        Long totalUsers = userRepository.count();
        Long totalShops = shopRepository.countByStatus(ShopStatus.APPROVED);
        BigDecimal platformRevenue = orderRepository.sumPlatformRevenueByDate(startDate, endDate);
        Long pendingComplaints = complaintRepository.countByStatus(ComplaintStatus.PENDING);

        Long pendingShops = shopRepository.countByStatus(ShopStatus.PENDING);
        Long pendingProducts = productRepository.countByStatus(com.ecommerce.backend.enums.ProductStatus.PENDING);
        BigDecimal totalGMV = orderRepository.sumGMVByDate(startDate, endDate);
        Long totalOrders = (long) orderRepository.countByCreatedAtBetween(startDate, endDate);
        Long activeCoupons = couponRepository
                .countByStatusAndIsDeletedFalse(com.ecommerce.backend.enums.CouponStatus.ACTIVE);

        return AdminDashboardKPIResponse.builder()
                .totalUsers(totalUsers)
                .totalShops(totalShops)
                .totalPlatformRevenue(platformRevenue != null ? platformRevenue : BigDecimal.ZERO)
                .pendingComplaints(pendingComplaints)
                .pendingShops(pendingShops)
                .pendingProducts(pendingProducts)
                .totalGMV(totalGMV != null ? totalGMV : BigDecimal.ZERO)
                .totalOrders(totalOrders)
                .activeCoupons(activeCoupons)
                .build();
    }

    public List<AdminRevenueChartResponse> getRevenueChart(ChartType type) {
        List<Object[]> rawData;

        switch (type) {
            case MONTH -> rawData = orderRepository.getPlatformRevenueByMonth();
            case YEAR -> rawData = orderRepository.getPlatformRevenueByYear();
            default -> rawData = orderRepository.getPlatformRevenueLast7Days();
        }

        Map<String, BigDecimal> map = new HashMap<>();
        for (Object[] row : rawData) {
            map.put(String.valueOf(row[0]), (BigDecimal) row[1]);
        }

        return fillMissingData(map, type);
    }

    private List<AdminRevenueChartResponse> fillMissingData(Map<String, BigDecimal> map, ChartType type) {
        List<AdminRevenueChartResponse> result = new ArrayList<>();

        switch (type) {
            case DAY -> {
                for (int i = 6; i >= 0; i--) {
                    LocalDate date = LocalDate.now().minusDays(i);
                    String key = date.toString();
                    result.add(new AdminRevenueChartResponse(key, map.getOrDefault(key, BigDecimal.ZERO)));
                }
            }
            case MONTH -> {
                java.time.YearMonth now = java.time.YearMonth.now();
                for (int i = 5; i >= 0; i--) {
                    java.time.YearMonth ym = now.minusMonths(i);
                    String key = ym.toString();
                    result.add(new AdminRevenueChartResponse(key, map.getOrDefault(key, BigDecimal.ZERO)));
                }
            }
            case YEAR -> {
                int currentYear = LocalDate.now().getYear();
                for (int i = 4; i >= 0; i--) {
                    int year = currentYear - i;
                    String key = String.valueOf(year);
                    result.add(new AdminRevenueChartResponse(key, map.getOrDefault(key, BigDecimal.ZERO)));
                }
            }
        }
        return result;
    }

    public List<AdminOrderStatusChartResponse> getOrderStatusChart(DateRange range) {
        DateRangeResult dateRange = DateRangeUtil.getRange(range);

        return orderRepository.countOrdersByStatusAndDate(dateRange.start(), dateRange.end())
                .stream()
                .map(row -> new AdminOrderStatusChartResponse(row[0].toString(), (Long) row[1]))
                .collect(java.util.stream.Collectors.toList());
    }

    public List<AdminCategorySalesChartResponse> getCategorySalesChart(DateRange range) {
        DateRangeResult dateRange = DateRangeUtil.getRange(range);

        return orderRepository.getCategorySalesByDate(dateRange.start(), dateRange.end())
                .stream()
                .map(row -> new AdminCategorySalesChartResponse(row[0].toString(), new BigDecimal(row[1].toString())))
                .collect(java.util.stream.Collectors.toList());
    }

    public List<AdminTopShopResponse> getTopSellingShops(DateRange range) {
        DateRangeResult dateRange = DateRangeUtil.getRange(range);

        return orderRepository.adminFindTopShopsByRevenueByDate(dateRange.start(), dateRange.end(), PageRequest.of(0, 3));
    }

    public AdminDashboardTopProductResponse getTopSellingProducts(DateRange range) {
        DateRangeResult dateRange = DateRangeUtil.getRange(range);

        return AdminDashboardTopProductResponse.builder()
                .topByRevenue(orderItemRepository.adminFindTopByRevenueByDate(dateRange.start(), dateRange.end(), PageRequest.of(0, 3)))
                .topBySold(orderItemRepository.adminFindTopBySoldByDate(dateRange.start(), dateRange.end(), PageRequest.of(0, 3)))
                .build();
    }
}
