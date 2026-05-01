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

    @GetMapping("/user/{userId}")
    public List<RecommendationResponse> getByUser(@PathVariable Integer userId) {
        return service.getByUser(userId);
    }

    @PostMapping
    public RecommendationResponse create(@RequestBody @Valid RecommendationRequest request) {
        return service.createRecommendation(request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.deleteRecommendation(id);
    }
}