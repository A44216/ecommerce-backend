package com.ecommerce.backend.dto.responses.admin.order;

import com.ecommerce.backend.enums.OrderStatus;
import com.ecommerce.backend.enums.PaymentMethod;
import com.ecommerce.backend.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminOrderDetailResponse {
    private Integer id;
    private String orderCode;
    private Integer userId;
    private String username;
    private Integer shopId;
    private String shopName;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal platformFeeRate;
    private BigDecimal platformFeeAmount;
    private BigDecimal totalPrice;
    private String shippingName;
    private String shippingPhone;
    private String shippingAddress;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    private List<AdminOrderItemResponse> items;
}
