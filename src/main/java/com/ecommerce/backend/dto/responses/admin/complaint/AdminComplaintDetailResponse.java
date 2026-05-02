package com.ecommerce.backend.dto.responses.admin.complaint;

import com.ecommerce.backend.dto.responses.admin.user.AdminUserResponse;
import com.ecommerce.backend.enums.ComplaintStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminComplaintDetailResponse {
    private Integer id;
    private String complaintCode;

    private String content;
    private ComplaintStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private AdminUserResponse resolvedBy;
    private String adminResponse;

    private AdminUserResponse user;

}
