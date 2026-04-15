package com.ecommerce.backend.dto.responses.admin.profile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminProfileInfoResponse {

    private String fullName;

    private String email;

    private String phone;

    private String avatar;
}
