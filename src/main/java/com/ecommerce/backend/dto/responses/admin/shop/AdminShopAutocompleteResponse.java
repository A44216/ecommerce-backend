package com.ecommerce.backend.dto.responses.admin.shop;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminShopAutocompleteResponse {
    private Integer id;
    private String name;
}