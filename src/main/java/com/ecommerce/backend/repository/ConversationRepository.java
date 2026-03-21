package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Integer> {

    // tất cả conversation của customer
    List<Conversation> findByCustomerId(Integer customerId);

    // conversation của shop
    List<Conversation> findByShopId(Integer shopId);

    // tìm chat giữa customer và shop
    Optional<Conversation> findByCustomerIdAndShopId(Integer customerId, Integer shopId);
}