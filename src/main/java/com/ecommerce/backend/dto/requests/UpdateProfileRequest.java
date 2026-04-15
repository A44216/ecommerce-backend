package com.ecommerce.backend.dto.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {
    private String fullName;
    private String email;
    private String phone;
}