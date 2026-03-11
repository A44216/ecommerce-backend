package com.ecommerce.backend.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ConversationResponse {

    private Integer id;

    private Integer customerId;
    private String customerName;

    private Integer shopId;
    private String shopName;

    private LocalDateTime createdAt;

}