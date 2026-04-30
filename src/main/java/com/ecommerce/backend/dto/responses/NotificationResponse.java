package com.ecommerce.backend.dto.responses;

import com.ecommerce.backend.enums.NotificationType;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Getter
@Setter
public class NotificationResponse {
    private Integer id;
    private String title;
    private String body;
    private NotificationType type;
    private Integer relatedId;
    private boolean isRead;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}