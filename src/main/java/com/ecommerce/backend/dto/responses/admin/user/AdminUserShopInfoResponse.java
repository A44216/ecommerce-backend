package com.ecommerce.backend.dto.responses.admin.user;

import com.ecommerce.backend.enums.ShopStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserShopInfoResponse {

    private Integer id;
    private String shopName;
    private ShopStatus status;
    private String description;

    private BigDecimal ratingAvg;
    private Integer totalOrders;
    private BigDecimal totalRevenue;
}
