package com.ecommerce.backend.dto.requests;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String email; // Android sẽ tự động gửi kèm cái này (người dùng không cần nhập lại)
    private String newPassword;
}