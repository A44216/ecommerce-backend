package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {

    // tất cả tin nhắn trong 1 conversation (ẩn tin nhắn AI Chatbot)
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND (m.isAiChat IS NULL OR m.isAiChat = false) ORDER BY m.createdAt ASC")
    List<Message> findByConversationIdOrderByCreatedAtAsc(@Param("conversationId") Integer conversationId);

    // lấy tin nhắn trong khoảng thời gian (để lấy 48h, ẩn AI chatbot)
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND (m.isAiChat IS NULL OR m.isAiChat = false) AND m.createdAt > :time ORDER BY m.createdAt ASC")
    List<Message> findByConversationIdAndCreatedAtAfterOrderByCreatedAtAsc(@Param("conversationId") Integer conversationId, @Param("time") java.time.LocalDateTime time);

    // lấy n tin nhắn cũ hơn một mốc thời gian (để load more, ẩn AI chatbot)
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND (m.isAiChat IS NULL OR m.isAiChat = false) AND m.createdAt < :time ORDER BY m.createdAt DESC")
    List<Message> findOlderMessages(@Param("conversationId") Integer conversationId, @Param("time") java.time.LocalDateTime time, org.springframework.data.domain.Pageable pageable);

    // lấy riêng tin nhắn AI Chatbot
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.isAiChat = true ORDER BY m.createdAt ASC")
    List<Message> findAiChatMessages(@Param("conversationId") Integer conversationId);

    // lấy riêng tin nhắn AI Chatbot trong khoảng thời gian
    List<Message> findByConversationIdAndIsAiChatAndCreatedAtAfterOrderByCreatedAtAsc(Integer conversationId, Boolean isAiChat, java.time.LocalDateTime time);

    // lấy riêng tin nhắn AI Chatbot cũ hơn (load more)
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.isAiChat = true AND m.createdAt < :time ORDER BY m.createdAt DESC")
    List<Message> findOlderAiMessages(@Param("conversationId") Integer conversationId, @Param("time") java.time.LocalDateTime time, org.springframework.data.domain.Pageable pageable);

    // tin nhắn của user
    List<Message> findBySenderId(Integer senderId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.customer.id = :customerId AND m.sender.id != :customerId AND m.isRead = false")
    Integer countUnreadForCustomer(@Param("customerId") Integer customerId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.shop.id = :shopId AND m.sender.id != m.conversation.shop.user.id AND m.isRead = false")
    Integer countUnreadForShop(@Param("shopId") Integer shopId);

    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.conversation.id = :conversationId AND m.sender.id != :userId AND m.isRead = false")
    void markMessagesAsRead(@Param("conversationId") Integer conversationId, @Param("userId") Integer userId);
}