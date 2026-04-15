package com.ecommerce.backend.dto.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConversationRequest {

    @NotNull(message = "Shop id is required")
    private Integer shopId;


    @NotNull(message = "Customer id is required")
    private Integer customerId;
}