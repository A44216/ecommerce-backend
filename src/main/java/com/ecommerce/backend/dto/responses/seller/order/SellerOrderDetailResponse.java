package com.ecommerce.backend.dto.responses.seller.order;

import com.ecommerce.backend.enums.OrderStatus;
import com.ecommerce.backend.enums.PaymentMethod;
import com.ecommerce.backend.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellerOrderDetailResponse {

    private Integer orderId;

    private OrderStatus status;

    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;

    private BigDecimal totalPrice;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    private String shippingName;
    private String shippingPhone;
    private String shippingAddress;

    private String customerName;

    private BigDecimal subtotal;

    private String couponCode;
    private String discountDescription;
    private BigDecimal discountAmount;

    private BigDecimal platformFeeRate;
    private BigDecimal platformFeeAmount;

    private List<SellerOrderItemResponse> items;

}