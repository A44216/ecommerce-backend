package com.ecommerce.backend.dto.responses;

import com.ecommerce.backend.enums.Role;
import com.ecommerce.backend.enums.UserStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserResponse {

    private Integer id;
    private String fullName;
    private String username;
    private String email;
    private String phone;
    private Role role;
    private UserStatus status;
    private LocalDateTime createdAt;
    private String avatar;
}