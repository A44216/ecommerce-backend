package com.ecommerce.backend.dto.responses;

import com.ecommerce.backend.enums.ShopStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class ShopResponse {

    private Integer id;
    private String shopName;
    private String description;
    private String ownerName;
    private ShopStatus status;
    private LocalDateTime createdAt;
}