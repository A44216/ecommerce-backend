package com.ecommerce.backend.dto.responses.admin.shop;

import com.ecommerce.backend.enums.ShopStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminShopResponse {
    private Integer id;
    private String shopName;
    private String email;
    private String phone;
    private ShopStatus status;
    private String avatar;
    private BigDecimal ratingAvg;
    private Integer totalOrders;
    private BigDecimal totalRevenue;
    private LocalDateTime createdAt;
}
