package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {

    // tất cả tin nhắn trong 1 conversation
    List<Message> findByConversationIdOrderByCreatedAtAsc(Integer conversationId);

    // tin nhắn của user
    List<Message> findBySenderId(Integer senderId);

}