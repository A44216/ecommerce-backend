package com.ecommerce.backend.dto.requests;

import lombok.Data;

@Data // Dùng @Data của Lombok để tự động tạo Getter/Setter
public class SendOtpRequest {
    private String email;
}