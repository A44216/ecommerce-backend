package com.ecommerce.backend.dto.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecommendationRequest {

    @NotNull(message = "User ID is required")
    private Integer userId;

    @NotNull(message = "Product ID is required")
    private Integer productId;

}