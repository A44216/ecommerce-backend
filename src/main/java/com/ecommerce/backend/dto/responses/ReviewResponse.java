package com.ecommerce.backend.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class ReviewResponse {

    private Integer id;
    private String userName;
    private Integer productId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

}