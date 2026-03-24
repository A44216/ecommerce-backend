package com.ecommerce.backend.dto.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleLoginRequest {
    private String email;
    private String name;
    private String googleId;
}