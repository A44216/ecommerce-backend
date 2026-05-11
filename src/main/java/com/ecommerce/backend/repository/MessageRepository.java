package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {

    // tất cả tin nhắn trong 1 conversation
    List<Message> findByConversationIdOrderByCreatedAtAsc(Integer conversationId);

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