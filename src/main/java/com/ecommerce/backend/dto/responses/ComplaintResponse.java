package com.ecommerce.backend.dto.responses;

import com.ecommerce.backend.enums.ComplaintStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Setter
public class ComplaintResponse {

    private Integer id;

    private Integer userId;

    private String content;

    private ComplaintStatus status;

    private String complaintCode;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

}