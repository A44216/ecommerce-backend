package com.ecommerce.backend.dto.responses.admin.user;

import com.ecommerce.backend.enums.Role;
import com.ecommerce.backend.enums.UserStatus;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse {

    private Integer id;
    private String username;
    private String fullName;
    private String email;
    private String phone;

    private Role role;      // CUSTOMER / SELLER
    private UserStatus status;    // ACTIVE / BLOCKED

    private String avatar;
}