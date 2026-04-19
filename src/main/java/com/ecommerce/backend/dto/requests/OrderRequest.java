package com.ecommerce.backend.dto.requests;

import com.ecommerce.backend.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class OrderRequest {

    @NotNull(message = "AddressId is required")
    private Integer addressId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private Integer couponId;

    @NotNull(message = "User id is required")
    private Integer userId;

    @NotNull(message = "Total price is required")
    private java.math.BigDecimal totalPrice;

    private java.math.BigDecimal subtotal;

    private java.math.BigDecimal discountAmount;

    @NotNull(message = "Shop id is required")
    private Integer shopId;

    private List<OrderItemRequest> orderItems;

    // Class con chứa thông tin từng món hàng
    @Getter
    @Setter
    public static class OrderItemRequest {
        private Integer productId;
        private Integer quantity;
        private BigDecimal price;
    }
}