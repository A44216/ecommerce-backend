package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.requests.AiChatRequest;
import com.ecommerce.backend.service.AiChatService;
import com.ecommerce.backend.entity.Shop;
import com.ecommerce.backend.repository.ShopRepository;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-chat")
@CrossOrigin
public class AiChatController {

    private final AiChatService aiChatService;
    private final ShopRepository shopRepository;

    public AiChatController(AiChatService aiChatService, ShopRepository shopRepository) {
        this.aiChatService = aiChatService;
        this.shopRepository = shopRepository;
    }

    @PostMapping("/ask")
    public Map<String, String> askAi(@RequestBody AiChatRequest request) {
        Shop shop = shopRepository.findById(request.getShopId())
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        
        String response = aiChatService.generateResponse(shop, request.getMessage());
        return Map.of("reply", response);
    }
}
