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
    private String fullName;
    private Integer shopId;
    private String shopName;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private BigDecimal subtotal;          // tổng tiền hàng
    private BigDecimal discountAmount;    // giảm giá
    private BigDecimal totalPrice;        // khách trả
    private BigDecimal platformFeeAmount; // phí hệ thống
    private BigDecimal sellerReceived;    // shop nhận
    private BigDecimal shippingFee;       // phí vận chuyển (computed)
    private BigDecimal platformFeeRate;
    private String shippingName;
    private String shippingPhone;
    private String shippingAddress;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    private List<AdminOrderItemResponse> items;
}
