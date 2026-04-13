package com.ecommerce.backend.dto.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {
    private String otpCode;
    private String newPassword;
}