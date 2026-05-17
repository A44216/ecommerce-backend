package com.ecommerce.backend.dto.requests;

import lombok.Data;

@Data
public class AiChatRequest {
    private Integer shopId;
    private Integer conversationId;
    private Integer senderId;
    private String message;
}
