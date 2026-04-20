package com.ecommerce.backend.dto.responses.admin.order;

import com.ecommerce.backend.enums.OrderStatus;
import com.ecommerce.backend.enums.PaymentMethod;
import com.ecommerce.backend.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminOrderResponse {
    private Integer id;
    private Integer userId;
    private String username;
    private Integer shopId;
    private String shopName;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private BigDecimal totalPrice;
    private BigDecimal platformFeeAmount;
    private LocalDateTime createdAt;
}
