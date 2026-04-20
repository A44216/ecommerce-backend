package com.ecommerce.backend.dto.responses.admin.shop;

import com.ecommerce.backend.dto.responses.admin.user.AdminUserResponse;
import com.ecommerce.backend.enums.ShopStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminShopDetailResponse {
    private Integer id;
    private String shopName;
    private String description;
    private String email;
    private String phone;
    private String address;
    private ShopStatus status;
    private String avatar;
    private BigDecimal ratingAvg;
    private Integer ratingCount;
    private Integer totalProducts;
    private Integer totalOrders;
    private BigDecimal totalRevenue;
    private LocalDateTime createdAt;

    private AdminUserResponse owner;
}
