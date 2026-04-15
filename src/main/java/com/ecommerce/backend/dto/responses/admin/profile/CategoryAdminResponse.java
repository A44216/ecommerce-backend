package com.ecommerce.backend.dto.responses.admin.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryAdminResponse {

    private Integer id;
    private String name;
    private Boolean isDeleted;
}
