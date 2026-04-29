package com.ecommerce.backend.dto.responses.admin.action;

import com.ecommerce.backend.enums.AdminActionType;
import com.ecommerce.backend.enums.EntityType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AdminActionResponse {
    private Integer id;
    private EntityType entityType;
    private Integer entityId;
    private AdminActionType action;
    private String reason;
    private LocalDateTime createdAt;
}
