package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.requests.AssistantChatRequest;
import com.ecommerce.backend.dto.responses.AssistantChatResponse;
import com.ecommerce.backend.service.CustomerAssistantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/assistant")
public class AssistantController {

    private final CustomerAssistantService customerAssistantService;

    public AssistantController(CustomerAssistantService customerAssistantService) {
        this.customerAssistantService = customerAssistantService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AssistantChatResponse> chat(@RequestBody AssistantChatRequest request) {
        AssistantChatResponse response = customerAssistantService.processChat(request);
        return ResponseEntity.ok(response);
    }
}
