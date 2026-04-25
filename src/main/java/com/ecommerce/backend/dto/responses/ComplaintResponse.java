package com.ecommerce.backend.dto.responses;

import com.ecommerce.backend.enums.ComplaintStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ComplaintResponse {
    private Integer id;
    private Integer orderId;
    private String content;
    private ComplaintStatus status;
    private LocalDateTime createdAt;
}