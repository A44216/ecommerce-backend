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

}