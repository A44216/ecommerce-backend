package com.ecommerce.backend.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssistantChatRequest {
    private List<MessageContextDTO> history = new ArrayList<>();
    private String message;
}
