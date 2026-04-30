package com.ecommerce.backend.dto.requests;

import com.ecommerce.backend.enums.ShopStatus;
import jakarta.validation.constraints.*;
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

    @NotNull(message = "User ID is required")
    private Integer userId;

    // Nếu chữ ShopStatus bị đỏ, bạn ấn Alt + Enter để import từ thư mục enums nhé
    private ShopStatus status;
}