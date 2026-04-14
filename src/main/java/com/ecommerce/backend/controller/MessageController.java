package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.responses.MessageResponse;
import com.ecommerce.backend.entity.Message;
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
    public Message sendMessage(@RequestBody Message message) {
        return service.sendMessage(message);
    }

    // xoá tin nhắn
    @DeleteMapping("/{id}")
    public void deleteMessage(@PathVariable Integer id) {
        service.deleteMessage(id);
    }
}