package com.ecommerce.backend.dto.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartRequest {

    @NotNull(message = "User id is required")
    private Integer userId;

}