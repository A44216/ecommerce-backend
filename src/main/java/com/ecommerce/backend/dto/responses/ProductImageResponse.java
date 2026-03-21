package com.ecommerce.backend.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ProductImageResponse {

    private Integer id;

    private Integer productId;

    private String imageUrl;

}