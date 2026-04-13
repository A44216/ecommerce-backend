package com.ecommerce.backend.dto.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleLoginRequest {
    // không dùng các trường này để xác thực bảo mật nữa
    private String email;
    private String name;
    private String googleId;

    // dùng cái này
    private String idToken;
}