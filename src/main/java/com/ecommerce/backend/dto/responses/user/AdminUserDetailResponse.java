package com.ecommerce.backend.dto.responses.user;

import com.ecommerce.backend.enums.Provider;
import com.ecommerce.backend.enums.Role;
import com.ecommerce.backend.enums.UserStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDetailResponse {

    private Integer id;
    private String fullName;
    private String username;
    private String email;
    private String phone;

    private Role role;
    private UserStatus status;
    private String avatar;

    private Provider provider;
    private LocalDateTime createdAt;

    private AdminUserShopInfoResponse shop;
}
