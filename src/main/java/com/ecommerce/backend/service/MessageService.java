package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.responses.MessageResponse;
import com.ecommerce.backend.entity.Message;
import com.ecommerce.backend.repository.MessageRepository;
import org.springframework.stereotype.Service;

import com.ecommerce.backend.dto.requests.MessageRequest;
import com.ecommerce.backend.entity.Conversation;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.ConversationRepository;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class MessageService {

    private final MessageRepository repository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final AiChatService aiChatService;

    public MessageService(MessageRepository repository, ConversationRepository conversationRepository, UserRepository userRepository, AiChatService aiChatService) {
        this.repository = repository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.aiChatService = aiChatService;
    }

    private MessageResponse mapToDTO(Message message) {

        return new MessageResponse(
                message.getId(),
                message.getConversation().getId(),
                message.getSender().getId(),
                message.getSender().getUsername(),
                message.getMessage(),
                message.getCreatedAt(),
                message.getIsRead(),
                message.getIsAiGenerated()
        );
    }

    // tin nhắn theo conversation
    public List<MessageResponse> getMessagesByConversation(Integer conversationId) {

        List<Message> list = repository.findByConversationIdOrderByCreatedAtAsc(conversationId);

        return list.stream()
                .map(this::mapToDTO)
                .toList();
    }

    // gửi tin nhắn
    @Transactional
    public MessageResponse sendMessage(MessageRequest request) {
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setMessage(request.getMessage());
        message.setCreatedAt(LocalDateTime.now());

        message = repository.save(message);

        // Kích hoạt AI Auto-reply nếu người gửi là Customer và Shop có bật AI
        if (conversation.getCustomer().getId().equals(sender.getId()) && 
            conversation.getShop().getIsAiReplyEnabled()) {
            triggerAiAutoReply(conversation, request.getMessage());
        }

        return mapToDTO(message);
    }

    @org.springframework.scheduling.annotation.Async
    public void triggerAiAutoReply(Conversation conversation, String userMessage) {
        // Giả lập độ trễ nhỏ để tạo cảm giác tự nhiên
        try { Thread.sleep(2000); } catch (InterruptedException e) {}

        String aiResponse = aiChatService.generateResponse(conversation.getShop(), userMessage);

        Message aiMessage = new Message();
        aiMessage.setConversation(conversation);
        aiMessage.setSender(conversation.getShop().getUser()); // Shop owner gửi
        aiMessage.setMessage(aiResponse);
        aiMessage.setCreatedAt(LocalDateTime.now());
        aiMessage.setIsAiGenerated(true);

        repository.save(aiMessage);
        
        // Lưu ý: Trong thực tế bạn có thể cần gửi qua WebSocket ở đây để người dùng thấy ngay lập tức
    }

    // xoá tin nhắn
    @Transactional
    public void deleteMessage(Integer id) {
        repository.deleteById(id);
    }

    public Integer countUnreadForCustomer(Integer customerId) {
        return repository.countUnreadForCustomer(customerId);
    }

    public Integer countUnreadForShop(Integer shopId) {
        return repository.countUnreadForShop(shopId);
    }

    @Transactional
    public void markMessagesAsRead(Integer conversationId, Integer userId) {
        repository.markMessagesAsRead(conversationId, userId);
    }
}