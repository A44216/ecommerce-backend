package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.responses.MessageResponse;
import com.ecommerce.backend.entity.Message;
import com.ecommerce.backend.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    private final MessageRepository repository;

    public MessageService(MessageRepository repository) {
        this.repository = repository;
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
    public Message sendMessage(Message message) {
        return repository.save(message);
    }

    // xoá tin nhắn
    public void deleteMessage(Integer id) {
        repository.deleteById(id);
    }
}