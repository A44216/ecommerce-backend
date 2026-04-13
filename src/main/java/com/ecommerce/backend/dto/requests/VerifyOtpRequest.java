package com.ecommerce.backend.dto.requests;
import lombok.Data;

@Data
public class VerifyOtpRequest {
    private String email;
    private String otpCode;
}