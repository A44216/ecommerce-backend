package com.ecommerce.backend.dto.requests;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewRequest {

    @NotNull(message = "Product ID is required")
    private Integer productId;

    @NotNull(message = "Order item ID is required")
    private Integer orderItemId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    @Size(max = 1000, message = "Comment must be less than 1000 characters")
    private String comment;

    @NotNull(message = "User ID is required")
    private Integer userId;
}