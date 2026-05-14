package com.ecommerce.backend.dto.responses.seller.shop;

import com.ecommerce.backend.enums.ShopStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class SellerShopResponse {

    private Integer id;
    private String shopName;
    private String description;
    private String ownerName;
    private ShopStatus status;
    private LocalDateTime createdAt;
    private String avatar;
    private String address;
    private BigDecimal ratingAvg;
    private Integer ratingCount;
    private String phone;
    private String email;
    private Boolean isAiReplyEnabled;
}