package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.RecommendationRequest;
import com.ecommerce.backend.dto.responses.RecommendationResponse;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.Recommendation;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.RecommendationRepository;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public RecommendationService(RecommendationRepository recommendationRepository,
                                 UserRepository userRepository,
                                 ProductRepository productRepository) {
        this.recommendationRepository = recommendationRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    private RecommendationResponse mapToDTO(Recommendation recommendation) {
        return RecommendationResponse.builder()
                .productId(recommendation.getProduct().getId())
                .productName(recommendation.getProduct().getName())
                // giả sử Entity Product dùng biến image lưu link ảnh giống file ProductService
                .imageUrl(recommendation.getProduct().getImage())
                .price(recommendation.getProduct().getPrice())
                .score(recommendation.getScore())
                .build();
    }

    private Recommendation getRecommendationOrThrow(Integer id) {
        return recommendationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Recommendation not found with id: " + id));
    }

    private User getUserOrThrow(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + id));
    }

    private Product getProductOrThrow(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found with id: " + id));
    }

    // tất cả recommendation
    public List<RecommendationResponse> getAllRecommendations() {
        return recommendationRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // recommendation theo user
    public List<RecommendationResponse> getByUser(Integer userId) {
        return recommendationRepository.findByUserId(userId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // tạo recommendation
    public RecommendationResponse createRecommendation(RecommendationRequest request) {

        User user = getUserOrThrow(request.getUserId());
        Product product = getProductOrThrow(request.getProductId());
        Recommendation recommendation = new Recommendation();
        recommendation.setUser(user);
        recommendation.setProduct(product);
        recommendation.setScore(request.getScore());

        return mapToDTO(recommendationRepository.save(recommendation));
    }

    // xóa
    public void deleteRecommendation(Integer id) {

        Recommendation recommendation = getRecommendationOrThrow(id);

        recommendationRepository.delete(recommendation);
    }
}