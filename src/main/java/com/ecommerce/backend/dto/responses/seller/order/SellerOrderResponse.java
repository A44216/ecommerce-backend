package com.ecommerce.backend.dto.responses.seller.order;

import com.ecommerce.backend.enums.OrderStatus;
import com.ecommerce.backend.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class SellerOrderResponse {

    private Integer orderId;
    private String orderCode;

    private OrderStatus status;
    private String customerName;
    private BigDecimal sellerRevenue;
    private LocalDateTime createdAt;

    private PaymentStatus paymentStatus;

    private String imageOrder;

}