package com.ecommerce.backend.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MessageResponse {

    private Integer id;

    private Integer conversationId;

    private Integer senderId;

    private String senderName;

    private String message;

    private LocalDateTime createdAt;

    private Boolean isRead;

    private Boolean isAiGenerated;

}