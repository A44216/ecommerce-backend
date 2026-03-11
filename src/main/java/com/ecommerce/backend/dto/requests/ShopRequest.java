package com.ecommerce.backend.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopRequest {

    @NotBlank(message = "Shop name is required")
    @Size(max = 100, message = "Shop name must be less than 100 characters")
    private String shopName;

    @Size(max = 1000, message = "Description is too long")
    private String description;
}