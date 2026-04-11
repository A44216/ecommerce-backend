package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.requests.RecommendationRequest;
import com.ecommerce.backend.dto.responses.RecommendationResponse;
import com.ecommerce.backend.service.RecommendationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    // tất cả recommendation
    @GetMapping
    public List<RecommendationResponse> getAllRecommendations() {
        return recommendationService.getAllRecommendations();
    }

    // recommendation theo user
    @GetMapping("/user/{userId}")
    public List<RecommendationResponse> getByUser(@PathVariable Integer userId) {
        return recommendationService.getByUser(userId);
    }

    // tạo recommendation
    @PostMapping
    public RecommendationResponse createRecommendation(
            @Valid @RequestBody RecommendationRequest request) {
        return recommendationService.createRecommendation(request);
    }

    // xóa
    @DeleteMapping("/{id}")
    public void deleteRecommendation(@PathVariable Integer id) {
        recommendationService.deleteRecommendation(id);
    }
}