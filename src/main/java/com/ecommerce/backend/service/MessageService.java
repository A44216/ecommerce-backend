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

    public MessageService(MessageRepository repository, ConversationRepository conversationRepository, UserRepository userRepository) {
        this.repository = repository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }

    private MessageResponse mapToDTO(Message message) {

        return new MessageResponse(
                message.getId(),
                message.getConversation().getId(),
                message.getSender().getId(),
                message.getSender().getUsername(),
                message.getMessage(),
                message.getCreatedAt()
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

        return mapToDTO(message);
    }

    // xoá tin nhắn
    @Transactional
    public void deleteMessage(Integer id) {
        repository.deleteById(id);
    }
}