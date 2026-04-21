package com.ecommerce.backend.dto.responses.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminTopShopResponse {
    private Integer id;
    private String shopName;
    private String avatar;
    private BigDecimal totalRevenue;
    private Integer totalOrders;

    public AdminTopShopResponse(Integer id, String shopName, String avatar, BigDecimal totalRevenue, Long totalOrders) {
        this.id = id;
        this.shopName = shopName;
        this.avatar = avatar;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.totalOrders = totalOrders != null ? totalOrders.intValue() : 0;
    }
}
