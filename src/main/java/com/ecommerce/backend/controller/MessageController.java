package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.responses.MessageResponse;

import com.ecommerce.backend.dto.requests.MessageRequest;

import com.ecommerce.backend.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin
public class MessageController {

    private final MessageService service;

    public MessageController(MessageService service) {
        this.service = service;
    }

    // tất cả tin nhắn trong conversation
    @GetMapping("/conversation/{conversationId}")
    public List<MessageResponse> getMessages(@PathVariable Integer conversationId) {
        return service.getMessagesByConversation(conversationId);
    }

    // gửi tin nhắn
    @PostMapping
    public MessageResponse sendMessage(@RequestBody MessageRequest request) {
        return service.sendMessage(request);
    }

    // xoá tin nhắn
    @DeleteMapping("/{id}")
    public void deleteMessage(@PathVariable Integer id) {
        service.deleteMessage(id);
    }

    @GetMapping("/unread-count/customer/{customerId}")
    public Integer getUnreadCountForCustomer(@PathVariable Integer customerId) {
        return service.countUnreadForCustomer(customerId);
    }

    @GetMapping("/unread-count/shop/{shopId}")
    public Integer getUnreadCountForShop(@PathVariable Integer shopId) {
        return service.countUnreadForShop(shopId);
    }

    @PutMapping("/read/{conversationId}")
    public void markMessagesAsRead(@PathVariable Integer conversationId, @RequestParam Integer userId) {
        service.markMessagesAsRead(conversationId, userId);
    }
}