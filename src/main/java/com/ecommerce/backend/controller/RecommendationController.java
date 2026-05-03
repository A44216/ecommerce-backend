package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.requests.RecommendationRequest;
import com.ecommerce.backend.dto.responses.RecommendationResponse;
import com.ecommerce.backend.service.RecommendationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService service;

    public RecommendationController(RecommendationService service) {
        this.service = service;
    }

    @GetMapping
    public List<RecommendationResponse> getAll() {
        return service.getAllRecommendations();
    }

    @GetMapping("/my-recommendations") // Endpoint mới không cần ID
    public List<RecommendationResponse> getMyRecommendations() {
        return service.getRecommendationsForCurrentUser();
    }

    @PostMapping
    public RecommendationResponse create(@RequestBody @Valid RecommendationRequest request) {
        // Truyền null cho tham số User để Service tự lấy người dùng đang đăng nhập qua Token
        return service.createRecommendation(request, null);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.deleteRecommendation(id);
    }
}