package com.ecommerce.backend.dto.requests;

import com.ecommerce.backend.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

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

    @NotNull(message = "Shop id is required")
    private Integer shopId;
}