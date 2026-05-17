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
                message.getIsAiGenerated(),
                message.getIsAiChat()
        );
    }

    // Lấy tin nhắn AI trong X giờ qua
    public List<MessageResponse> getRecentAiMessages(Integer conversationId, int hours) {
        LocalDateTime time = LocalDateTime.now().minusHours(hours);
        List<Message> list = repository.findByConversationIdAndIsAiChatAndCreatedAtAfterOrderByCreatedAtAsc(conversationId, true, time);
        return list.stream().map(this::mapToDTO).toList();
    }

    // Load more tin nhắn AI cũ hơn
    public List<MessageResponse> getOlderAiMessages(Integer conversationId, LocalDateTime time, int limit) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        List<Message> list = repository.findOlderAiMessages(conversationId, time, pageable);
        // DB trả về DESC, nên cần reverse lại cho đúng thứ tự chat ASC
        java.util.Collections.reverse(list);
        return list.stream().map(this::mapToDTO).toList();
    }

    // tin nhắn theo conversation
    public List<MessageResponse> getMessagesByConversation(Integer conversationId) {

        List<Message> list = repository.findByConversationIdOrderByCreatedAtAsc(conversationId);

        return list.stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<MessageResponse> getRecentMessages(Integer conversationId, int hours) {
        LocalDateTime time = LocalDateTime.now().minusHours(hours);
        List<Message> list = repository.findByConversationIdAndCreatedAtAfterOrderByCreatedAtAsc(conversationId, time);
        return list.stream().map(this::mapToDTO).toList();
    }

    public List<MessageResponse> getOlderMessages(Integer conversationId, LocalDateTime beforeTime, int limit) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        List<Message> list = repository.findOlderMessages(conversationId, beforeTime, pageable);
        
        // Vì query theo DESC để lấy tin nhắn ngay sát trước đó, nên ta cần đảo ngược list lại để đúng thứ tự hiển thị ASC
        java.util.Collections.reverse(list);
        
        return list.stream().map(this::mapToDTO).toList();
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

        // Kích hoạt Auto-reply nếu người gửi là Customer
        if (conversation.getCustomer().getId().equals(sender.getId())) {
            triggerAiAutoReply(conversation, message.getId());
        }

        return mapToDTO(message);
    }

    public void triggerAiAutoReply(Conversation conversation, Integer userMessageId) {
        Integer conversationId = conversation.getId();
        Integer shopUserId = conversation.getShop().getUser().getId();
        
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try { 
                // Chờ 6 giây. Nếu trong 6 giây này seller đang online trong phòng chat, 
                // polling của app sẽ gọi API markAsRead và chuyển isRead thành true.
                Thread.sleep(6000); 
            } catch (InterruptedException e) {}

            // Kiểm tra xem tin nhắn đã được đọc chưa
            Message userMsg = repository.findById(userMessageId).orElse(null);
            if (userMsg != null && Boolean.TRUE.equals(userMsg.getIsRead())) {
                // Seller đang online và đã đọc tin nhắn, không cần gửi auto-reply nữa
                return;
            }

            String aiResponse = "Shop đã nhận được tin nhắn của bạn, sẽ phản hồi lại sớm nhất có thể";

            Message aiMessage = new Message();
            aiMessage.setConversation(conversationRepository.findById(conversationId).orElse(null));
            aiMessage.setSender(userRepository.findById(shopUserId).orElse(null)); 
            aiMessage.setMessage(aiResponse);
            aiMessage.setCreatedAt(LocalDateTime.now());
            aiMessage.setIsAiGenerated(true);

            if (aiMessage.getConversation() != null && aiMessage.getSender() != null) {
                repository.save(aiMessage);
            }
        });
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