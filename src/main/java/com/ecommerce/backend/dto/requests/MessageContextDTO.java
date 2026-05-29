package com.ecommerce.backend.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageContextDTO {
    // "user" or "model"
    private String role;
    private String content;
}
