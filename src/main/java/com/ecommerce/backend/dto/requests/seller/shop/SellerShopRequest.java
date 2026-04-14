package com.ecommerce.backend.dto.requests.seller.shop;

import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellerShopRequest {

    @NotBlank(message = "Shop name is required")
    @Size(max = 100, message = "Shop name must be less than 100 characters")
    private String shopName;

    @Size(max = 1000, message = "Description is too long")
    private String description;

    @Size(max = 255, message = "Address is too long")
    private String address;

    @Pattern(regexp = "^0[0-9]{9,10}$", message = "Invalid phone number")
    private String phone;

    @Size(max = 100, message = "Email must be less than 100 characters")
    @Email
    private String email;

    @Size(max = 255, message = "Avatar URL is too long")
    private String avatar;
}