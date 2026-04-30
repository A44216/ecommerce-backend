package com.ecommerce.backend.dto.requests;

import lombok.Data;

@Data
public class ChangeEmailRequest {
    private String newEmail;
    private String oldEmailOtp;
    private String newEmailOtp;
}
