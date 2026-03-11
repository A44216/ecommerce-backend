package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.responses.ConversationResponse;
import com.ecommerce.backend.entity.Conversation;
import com.ecommerce.backend.repository.ConversationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationService {

    private final ConversationRepository repository;

    public ConversationService(ConversationRepository repository) {
        this.repository = repository;
    }

    private ConversationResponse mapToDTO(Conversation c) {

        return new ConversationResponse(
                c.getId(),
                c.getCustomer().getId(),
                c.getCustomer().getUsername(),
                c.getShop().getId(),
                c.getShop().getShopName(),
                c.getCreatedAt()
        );
    }

    // tất cả conversation
    public List<ConversationResponse> getAll() {

        List<Conversation> list = repository.findAll();

        return list.stream()
                .map(this::mapToDTO)
                .toList();
    }

    // theo id
    public ConversationResponse getById(Integer id) {

        Conversation c = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        return mapToDTO(c);
    }

    // theo customer
    public List<ConversationResponse> getByCustomer(Integer customerId) {

        List<Conversation> list = repository.findByCustomerId(customerId);

        return list.stream()
                .map(this::mapToDTO)
                .toList();
    }

    // theo shop
    public List<ConversationResponse> getByShop(Integer shopId) {

        List<Conversation> list = repository.findByShopId(shopId);

        return list.stream()
                .map(this::mapToDTO)
                .toList();
    }

    // tạo conversation
    public Conversation create(Conversation conversation) {
        return repository.save(conversation);
    }

    // xoá
    public void delete(Integer id) {
        repository.deleteById(id);
    }
}