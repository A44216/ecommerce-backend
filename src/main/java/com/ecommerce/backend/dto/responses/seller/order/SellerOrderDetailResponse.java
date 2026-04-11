package com.ecommerce.backend.dto.responses.seller.order;

import com.ecommerce.backend.enums.OrderStatus;
import com.ecommerce.backend.enums.PaymentMethod;
import com.ecommerce.backend.enums.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
public class SellerOrderDetailResponse {

    private Integer orderId;

    private OrderStatus status;

    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;

    private BigDecimal totalPrice;

    private String createdAt;

    private String shippingName;
    private String shippingPhone;
    private String shippingAddress;

    private String customerName;

    private List<SellerOrderItemResponse> items;

    public SellerOrderDetailResponse(Integer orderId, OrderStatus status,
                                     PaymentMethod paymentMethod,
                                     PaymentStatus paymentStatus,
                                     BigDecimal totalPrice,
                                     String createdAt,
                                     String shippingName,
                                     String shippingPhone,
                                     String shippingAddress,
                                     String customerName,
                                     List<SellerOrderItemResponse> items) {

        this.orderId = orderId;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
        this.shippingName = shippingName;
        this.shippingPhone = shippingPhone;
        this.shippingAddress = shippingAddress;
        this.customerName = customerName;
        this.items = items;
    }
}