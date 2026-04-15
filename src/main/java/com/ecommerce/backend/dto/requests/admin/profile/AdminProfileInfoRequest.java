package com.ecommerce.backend.dto.requests.admin.profile;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminProfileInfoRequest {
    private String fullName;

    private String email;

    private String phone;

    private String avatar;
}
