package com.ecommerce.backend.dto.requests;

import com.ecommerce.backend.enums.NotificationType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SystemNotificationRequest {
    private String title;
    private String body;
    private NotificationType type;
}
