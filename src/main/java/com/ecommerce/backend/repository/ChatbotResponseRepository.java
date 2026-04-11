package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.ChatbotResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatbotResponseRepository extends JpaRepository<ChatbotResponse, Integer> {

    Optional<ChatbotResponse> findByKeywordIgnoreCase(String keyword);

    boolean existsByKeywordIgnoreCase(String keyword);

    boolean existsByKeywordIgnoreCaseAndIdNot(String keyword, Integer id);
}