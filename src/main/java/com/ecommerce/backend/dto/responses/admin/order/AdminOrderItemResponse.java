package com.ecommerce.backend.dto.responses.admin.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AdminOrderItemResponse {
    private Integer id;
    private Integer productId;
    private String productCode;
    private String productName;
    private String productImage;
    private BigDecimal price;
    private Integer quantity;
}
