package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.ConversationRequest;
import com.ecommerce.backend.dto.responses.ConversationResponse;
import com.ecommerce.backend.entity.Conversation;
import com.ecommerce.backend.entity.Shop;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.repository.ConversationRepository;
import com.ecommerce.backend.repository.ShopRepository;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConversationService {

    private final ConversationRepository repository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    public ConversationService(ConversationRepository repository,
                               UserRepository userRepository,
                               ShopRepository shopRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.shopRepository = shopRepository;
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
        return list.stream().map(this::mapToDTO).toList();
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
        return list.stream().map(this::mapToDTO).toList();
    }

    // theo shop
    public List<ConversationResponse> getByShop(Integer shopId) {
        List<Conversation> list = repository.findByShopId(shopId);
        return list.stream().map(this::mapToDTO).toList();
    }

    // ==========================================
    // TẠO MỚI HOẶC TRẢ VỀ PHÒNG CHAT CŨ
    // ==========================================
    public ConversationResponse createConversation(ConversationRequest request) {
        // 1. Kiểm tra xem 2 người này đã có phòng chat chưa
        Optional<Conversation> existingOpt = repository.findByCustomerIdAndShopId(request.getCustomerId(), request.getShopId());

        if (existingOpt.isPresent()) {
            // Có rồi thì trả về phòng cũ (Để load lại tin nhắn cũ)
            return mapToDTO(existingOpt.get());
        }

        // 2. Nếu chưa có thì tạo phòng mới
        User customer = userRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Shop shop = shopRepository.findById(request.getShopId())
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        Conversation newConversation = new Conversation();
        newConversation.setCustomer(customer);
        newConversation.setShop(shop);

        Conversation saved = repository.save(newConversation);
        return mapToDTO(saved);
    }

    // xoá
    public void delete(Integer id) {
        repository.deleteById(id);
    }
}