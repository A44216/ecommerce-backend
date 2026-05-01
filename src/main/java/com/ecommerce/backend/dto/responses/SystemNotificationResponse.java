package com.ecommerce.backend.dto.responses;

import com.ecommerce.backend.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SystemNotificationResponse {
    private Integer id;
    private String title;
    private String body;
    private NotificationType type;
    private LocalDateTime createdAt;
}
