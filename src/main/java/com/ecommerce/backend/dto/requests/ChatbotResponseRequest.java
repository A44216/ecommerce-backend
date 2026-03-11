package com.ecommerce.backend.dto.requests;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatbotResponseRequest {

    @NotBlank(message = "Keyword is required")
    @Size(max = 100, message = "Keyword must be less than 100 characters")
    private String keyword;

    @NotBlank(message = "Response is required")
    @Size(max = 2000, message = "Response must be less than 2000 characters")
    private String response;
}