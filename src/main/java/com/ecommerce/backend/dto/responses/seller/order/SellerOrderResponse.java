package com.ecommerce.backend.dto.responses.seller.order;

import com.ecommerce.backend.enums.OrderStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class SellerOrderResponse {

    private Integer orderId;
    private OrderStatus status;
    private String customerName;
    private BigDecimal totalPrice;
    private String createdAt;

    private String paymentMethod;
    private String paymentStatus;

    private String imageOrder;

}