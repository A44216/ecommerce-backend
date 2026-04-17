package com.ecommerce.backend.dto.responses.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminCategoryResponse {

    private Integer id;
    private String name;
    private Boolean isDeleted;
}
