package com.ecommerce.backend.dto.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    private Integer id;
    private String username;
    private String email;
    private String role;

    private String token; // token JWT

}
