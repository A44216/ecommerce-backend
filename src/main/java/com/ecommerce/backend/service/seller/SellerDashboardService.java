package com.ecommerce.backend.service.seller;

import com.ecommerce.backend.dto.responses.seller.dashboard.SellerDashboardKPIResponse;
import com.ecommerce.backend.dto.responses.seller.dashboard.SellerDashboardTopProductResponse;
import com.ecommerce.backend.dto.responses.seller.dashboard.SellerRevenueChartResponse;
import com.ecommerce.backend.entity.Shop;
import com.ecommerce.backend.enums.ChartType;
import com.ecommerce.backend.enums.DateRange;
import com.ecommerce.backend.repository.OrderItemRepository;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.ShopRepository;
import com.ecommerce.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SellerDashboardService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;

    public List<SellerRevenueChartResponse> getRevenueChart(Authentication authentication, ChartType type) {

        Integer shopId = getShopId(authentication);

        List<Object[]> rawData;

        switch (type) {
            case MONTH -> rawData = orderRepository.getRevenueByMonth(shopId);
            case YEAR -> rawData = orderRepository.getRevenueByYear(shopId);
            default -> rawData = orderRepository.getRevenueLast7Days(shopId);
        }

        Map<String, BigDecimal> map = new HashMap<>();
        for (Object[] row : rawData) {
            map.put(String.valueOf(row[0]), (BigDecimal) row[1]);
        }

        return fillMissingData(map, type);
    }

    private Integer getShopId(Authentication authentication) {

        String username = authentication.getName();

        return shopRepository.findByUsername(username)
                .map(Shop::getId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
    }

    private List<SellerRevenueChartResponse> fillMissingData(
            Map<String, BigDecimal> map,
            ChartType type
    ) {
        List<SellerRevenueChartResponse> result = new ArrayList<>();

        switch (type) {

            case DAY -> {
                for (int i = 6; i >= 0; i--) {
                    LocalDate date = LocalDate.now().minusDays(i);
                    String key = date.toString();

                    result.add(new SellerRevenueChartResponse(
                            key,
                            map.getOrDefault(key, BigDecimal.ZERO)
                    ));
                }
            }

            case MONTH -> {
                java.time.YearMonth now = java.time.YearMonth.now();

                for (int i = 5; i >= 0; i--) {
                    java.time.YearMonth ym = now.minusMonths(i);
                    String key = ym.toString();

                    result.add(new SellerRevenueChartResponse(
                            key,
                            map.getOrDefault(key, BigDecimal.ZERO)
                    ));
                }
            }

            case YEAR -> {
                int currentYear = LocalDate.now().getYear();

                for (int i = 4; i >= 0; i--) {
                    int year = currentYear - i;
                    String key = String.valueOf(year);

                    result.add(new SellerRevenueChartResponse(
                            key,
                            map.getOrDefault(key, BigDecimal.ZERO)
                    ));
                }
            }
        }

        return result;
    }

    private LocalDateTime[] getDateRange(DateRange range) {

        LocalDateTime start;
        LocalDateTime end = LocalDateTime.now();

        switch (range) {
            case TODAY -> {
                start = LocalDate.now().atStartOfDay();
            }
            case LAST_7_DAYS -> {
                start = LocalDate.now().minusDays(6).atStartOfDay();
            }
            case LAST_30_DAYS -> {
                start = LocalDate.now().minusDays(29).atStartOfDay();
            }
            case THIS_MONTH -> {
                start = LocalDate.now().withDayOfMonth(1).atStartOfDay();
            }
            case LAST_MONTH -> {
                start = LocalDate.now().minusMonths(1).withDayOfMonth(1).atStartOfDay();
                end = LocalDate.now().withDayOfMonth(1).atStartOfDay().minusSeconds(1);
            }
            case LAST_3_MONTHS -> {
                start = LocalDate.now().minusMonths(3).withDayOfMonth(1).atStartOfDay();
            }
            case LAST_6_MONTHS -> {
                start = LocalDate.now().minusMonths(6).withDayOfMonth(1).atStartOfDay();
            }
            case THIS_YEAR -> {
                start = LocalDate.now().withDayOfYear(1).atStartOfDay();
            }
            default -> {
                start = LocalDate.now().atStartOfDay();
            }
        }

        return new LocalDateTime[]{start, end};
    }

    public SellerDashboardKPIResponse getKPI(
            Authentication authentication,
            DateRange range
    ) {
        Integer shopId = getShopId(authentication);

        LocalDateTime[] dateRange = getDateRange(range);
        LocalDateTime startDate = dateRange[0];
        LocalDateTime endDate = dateRange[1];

        BigDecimal revenue = orderRepository.sumRevenueByShopAndDate(shopId, startDate, endDate);
        Integer orders = orderRepository.countOrderByShopAndDate(shopId, startDate, endDate);
        Integer sold = orderItemRepository.sumQuantityByShopAndDate(shopId, startDate, endDate);

        return new SellerDashboardKPIResponse(
                revenue != null ? revenue : BigDecimal.ZERO,
                orders != null ? orders : 0,
                sold != null ? sold : 0
        );
    }

    public SellerDashboardTopProductResponse getTopProducts(
            Authentication authentication,
            DateRange range
    ) {
        Integer shopId = getShopId(authentication);

        LocalDateTime[] dateRange = getDateRange(range);
        LocalDateTime startDate = dateRange[0];
        LocalDateTime endDate = dateRange[1];

        return new SellerDashboardTopProductResponse(
                orderItemRepository.findTopByRevenueByDate(
                        shopId,
                        startDate,
                        endDate,
                        PageRequest.of(0, 3)
                ),
                orderItemRepository.findTopBySoldByDate(
                        shopId,
                        startDate,
                        endDate,
                        PageRequest.of(0, 3)
                )
        );
    }

}