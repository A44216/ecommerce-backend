package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.requests.ConversationRequest;
import com.ecommerce.backend.dto.responses.ConversationResponse;
import com.ecommerce.backend.entity.Conversation;
import com.ecommerce.backend.service.ConversationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@CrossOrigin
public class ConversationController {

    private final ConversationService service;

    public ConversationController(ConversationService service) {
        this.service = service;
    }

    // tất cả conversation
    @GetMapping
    public List<ConversationResponse> getAll() {
        return service.getAll();
    }

    // theo id
    @GetMapping("/{id}")
    public ConversationResponse getById(@PathVariable Integer id) {
        return service.getById(id);
    }

    // conversation theo customer
    @GetMapping("/customer/{customerId}")
    public List<ConversationResponse> getByCustomer(@PathVariable Integer customerId) {
        return service.getByCustomer(customerId);
    }

    // conversation theo shop
    @GetMapping("/shop/{shopId}")
    public List<ConversationResponse> getByShop(@PathVariable Integer shopId) {
        return service.getByShop(shopId);
    }

    // tạo conversation
    // Trong ConversationController.java
    @PostMapping
    public ConversationResponse create(@RequestBody ConversationRequest request) {
        return service.createConversation(request);
    }

    // xoá
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}