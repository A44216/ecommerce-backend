package com.ecommerce.backend.service.seller;

import com.ecommerce.backend.dto.responses.seller.dashboard.SellerDashboardResponse;
import com.ecommerce.backend.dto.responses.seller.dashboard.SellerRevenueChartResponse;
import com.ecommerce.backend.entity.Shop;
import com.ecommerce.backend.enums.ChartType;
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

    public SellerDashboardResponse getDashboard(Authentication authentication) {

        Integer shopId = getShopId(authentication);

        BigDecimal revenue = orderRepository.sumRevenueByShop(shopId);
        if (revenue == null) revenue = BigDecimal.ZERO;

        Integer orders = orderRepository.countByShopId(shopId);
        if (orders == null) orders = 0;

        Integer sold = orderItemRepository.sumQuantityByShopId(shopId);
        if (sold == null) sold = 0;

        return new SellerDashboardResponse(
                revenue,
                orders,
                sold,
                orderItemRepository.findTopByRevenue(shopId, PageRequest.of(0, 3)),
                orderItemRepository.findTopBySold(shopId, PageRequest.of(0, 3))
        );
    }

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
}