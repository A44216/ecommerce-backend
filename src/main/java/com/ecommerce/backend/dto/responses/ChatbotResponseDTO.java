package com.ecommerce.backend.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatbotResponseDTO {

    private Integer id;
    private String keyword;
    private String response;

}