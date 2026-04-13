package com.ecommerce.backend.dto.requests;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductImageRequest {

    @NotNull(message = "Product id is required")
    private Integer productId;

    @NotBlank(message = "Image URL is required")
    @Size(max = 255, message = "Image URL must be less than 255 characters")
    private String imageUrl;
}