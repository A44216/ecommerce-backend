package com.ecommerce.backend.dto.responses.admin.complaint;

import com.ecommerce.backend.enums.ComplaintStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminComplaintResponse {
    private Integer id;
    private String complaintCode;

    private String username;

    private String content;
    private ComplaintStatus status;
    private LocalDateTime createdAt;

    private Integer resolvedBy;
    private String adminResponse;
}
