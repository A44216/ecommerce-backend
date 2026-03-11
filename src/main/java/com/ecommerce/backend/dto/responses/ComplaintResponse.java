package com.ecommerce.backend.dto.responses;

import com.ecommerce.backend.enums.ComplaintStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ComplaintResponse {

    private Integer id;

    private Integer userId;

    private Integer orderId;

    private String content;

    private ComplaintStatus status;

    private LocalDateTime createdAt;

}