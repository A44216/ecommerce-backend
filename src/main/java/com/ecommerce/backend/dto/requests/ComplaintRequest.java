package com.ecommerce.backend.dto.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComplaintRequest {
    private Integer userId;
    private Integer orderId; // Có thể null nếu là khiếu nại chung
    private String content;
}