package com.ecommerce.backend.dto.responses;

import com.ecommerce.backend.enums.OrderStatus;
import com.ecommerce.backend.enums.PaymentMethod;
import com.ecommerce.backend.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Integer id;
    private Integer userId;
    private String username;

    private Integer shopId;

    private OrderStatus status;

    private BigDecimal totalPrice;

    private BigDecimal subtotal;

    private BigDecimal discountAmount;

    private BigDecimal platformFeeRate;

    private BigDecimal platformFeeAmount;

    private Integer couponId;

    private PaymentMethod paymentMethod;

    private PaymentStatus paymentStatus;

    private LocalDateTime createdAt;

    private String shippingName;
    private String shippingPhone;

    private String addressLine;
    private String city;
    private String district;
    private String ward;

    private List<OrderItemResponse> orderItems;

}