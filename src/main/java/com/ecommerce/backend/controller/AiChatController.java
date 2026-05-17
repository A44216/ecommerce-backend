package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.requests.AiChatRequest;
import com.ecommerce.backend.service.AiChatService;
import com.ecommerce.backend.entity.Shop;
import com.ecommerce.backend.repository.ShopRepository;
import org.springframework.web.bind.annotation.*;
import com.ecommerce.backend.dto.responses.MessageResponse;
import com.ecommerce.backend.entity.Conversation;
import com.ecommerce.backend.entity.Message;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.repository.ConversationRepository;
import com.ecommerce.backend.repository.MessageRepository;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/ai-chat")
@CrossOrigin
public class AiChatController {

    private final AiChatService aiChatService;
    private final ShopRepository shopRepository;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public AiChatController(AiChatService aiChatService, ShopRepository shopRepository, MessageRepository messageRepository, ConversationRepository conversationRepository, UserRepository userRepository) {
        this.aiChatService = aiChatService;
        this.shopRepository = shopRepository;
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/ask")
    public ResponseEntity<MessageResponse> askAi(@RequestBody AiChatRequest request) {
        Shop shop = shopRepository.findById(request.getShopId())
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Lưu tin nhắn của User
        Message userMsg = new Message();
        userMsg.setConversation(conversation);
        userMsg.setSender(sender);
        userMsg.setMessage(request.getMessage());
        userMsg.setCreatedAt(LocalDateTime.now());
        userMsg.setIsAiChat(true);
        messageRepository.save(userMsg);

        // Sinh response AI
        String aiResponse = aiChatService.generateResponse(shop, request.getMessage());

        // Lưu tin nhắn của AI
        Message aiMsg = new Message();
        aiMsg.setConversation(conversation);
        aiMsg.setSender(shop.getUser()); // AI đại diện cho Shop
        aiMsg.setMessage(aiResponse);
        aiMsg.setCreatedAt(LocalDateTime.now());
        aiMsg.setIsAiGenerated(true);
        aiMsg.setIsAiChat(true);
        aiMsg = messageRepository.save(aiMsg);

        // Chuyển thành DTO
        MessageResponse dto = new MessageResponse(
                aiMsg.getId(),
                aiMsg.getConversation().getId(),
                aiMsg.getSender().getId(),
                aiMsg.getSender().getUsername(),
                aiMsg.getMessage(),
                aiMsg.getCreatedAt(),
                aiMsg.getIsRead(),
                aiMsg.getIsAiGenerated(),
                aiMsg.getIsAiChat()
        );

        return ResponseEntity.ok(dto);
    }
}
