package com.ecommerce.backend.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssistantChatResponse {
    // e.g. "TEXT", "PRODUCT_CAROUSEL"
    private String type;
    
    // AI's message
    private String text;
    
    // Additional data like a List of Products
    private Object data;
}
