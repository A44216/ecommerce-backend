package com.ecommerce.backend.dto.requests.admin.profile;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminChangePasswordRequest {
    private String currentPassword;
    private String newPassword;
}
