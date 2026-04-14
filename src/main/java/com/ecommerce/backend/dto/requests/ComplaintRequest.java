package com.ecommerce.backend.dto.requests;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComplaintRequest {

    @NotNull(message = "Order id is required")
    private Integer orderId;

    @NotBlank(message = "Complaint content is required")
    @Size(max = 2000, message = "Content must be less than 2000 characters")
    private String content;

    @NotNull(message = "User id is required")
    private Integer userId;
}