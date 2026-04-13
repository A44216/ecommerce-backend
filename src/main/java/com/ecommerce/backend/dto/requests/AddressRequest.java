package com.ecommerce.backend.dto.requests;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must be less than 100 characters")
    private String fullName;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^0[0-9]{9,10}$", message = "Invalid phone number")
    private String phone;

    @NotBlank(message = "Address line is required")
    @Size(max = 255, message = "Address line must be less than 255 characters")
    private String addressLine;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must be less than 100 characters")
    private String city;

    @NotBlank(message = "District is required")
    @Size(max = 100, message = "District must be less than 100 characters")
    private String district;

    @NotBlank(message = "Ward is required")
    @Size(max = 100, message = "Ward must be less than 100 characters")
    private String ward;

    private Boolean isDefault = false;

    @NotNull(message = "User ID is required")
    private Integer userId;
}