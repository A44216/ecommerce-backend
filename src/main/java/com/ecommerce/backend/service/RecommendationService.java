package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.RecommendationRequest;
import com.ecommerce.backend.dto.responses.RecommendationResponse;
import com.ecommerce.backend.entity.*;
import com.ecommerce.backend.enums.RecommendationType;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public RecommendationService(
            RecommendationRepository recommendationRepository,
            UserRepository userRepository,
            ProductRepository productRepository
    ) {
        this.recommendationRepository = recommendationRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    // GET
    public List<RecommendationResponse> getAllRecommendations() {
        return recommendationRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<RecommendationResponse> getByUser(Integer userId) {
        return recommendationRepository.findByUserId(userId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // CREATE FUZZY CORE (FIXED)
    public RecommendationResponse createRecommendation(RecommendationRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // SAFE DATA HANDLING
        int soldCount = product.getSoldCount() == null ? 0 : product.getSoldCount();
        BigDecimal price = product.getPrice() == null ? BigDecimal.ZERO : product.getPrice();
        BigDecimal ratingAvg = product.getRatingAvg() == null ? BigDecimal.ZERO : product.getRatingAvg();

        // FUZZY SCORES
        BigDecimal soldScore = normalize(soldCount, 1000);
        BigDecimal ratingScore = ratingAvg.divide(BigDecimal.valueOf(5), 4, RoundingMode.HALF_UP);
        BigDecimal priceScoreValue = calculatePriceScore(price);

        // WEIGHTED SCORE (FUZZY MODEL)
        BigDecimal score = soldScore.multiply(BigDecimal.valueOf(0.4))
                .add(ratingScore.multiply(BigDecimal.valueOf(0.4)))
                .add(priceScoreValue.multiply(BigDecimal.valueOf(0.2)));

        // ENTITY
        Recommendation recommendation = new Recommendation();
        recommendation.setUser(user);
        recommendation.setProduct(product);
        recommendation.setSoldScore(soldScore);
        recommendation.setRatingScore(ratingScore);
        recommendation.setPriceScore(priceScoreValue);
        recommendation.setScore(score);
        recommendation.setType(RecommendationType.FUZZY);
        recommendation.setReason(buildReason(soldScore, ratingScore, priceScoreValue));

        return mapToDTO(recommendationRepository.save(recommendation));
    }

    // DELETE
    public void deleteRecommendation(Integer id) {
        Recommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation not found"));

        recommendationRepository.delete(recommendation);
    }

    // FUZZY FUNCTIONS
    private BigDecimal normalize(int value, int max) {
        if (max == 0) return BigDecimal.ZERO;

        return BigDecimal.valueOf(value)
                .divide(BigDecimal.valueOf(max), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculatePriceScore(BigDecimal price) {

        if (price.compareTo(BigDecimal.valueOf(1_000_000)) < 0) {
            return BigDecimal.ONE;
        } else if (price.compareTo(BigDecimal.valueOf(5_000_000)) < 0) {
            return BigDecimal.valueOf(0.6);
        } else {
            return BigDecimal.valueOf(0.3);
        }
    }

    // XAI (EXPLAINABLE AI)
    private String buildReason(BigDecimal sold, BigDecimal rating, BigDecimal price) {
        return "Recommended because: "
                + "high sales score (" + sold + "), "
                + "good rating (" + rating + "), "
                + "optimized price score (" + price + ")";
    }

    // MAPPER (SAFE + LAZY SAFE)
    private RecommendationResponse mapToDTO(Recommendation r) {

        String imageUrl = null;

        if (r.getProduct().getImages() != null && !r.getProduct().getImages().isEmpty()) {
            imageUrl = r.getProduct().getImages().get(0).getImageUrl();
        }

        return RecommendationResponse.builder()
                .productId(r.getProduct().getId())
                .productName(r.getProduct().getName())
                .imageUrl(imageUrl)
                .price(r.getProduct().getPrice())
                .score(r.getScore())
                .soldScore(r.getSoldScore())
                .ratingScore(r.getRatingScore())
                .priceScore(r.getPriceScore())
                .type(r.getType().name())
                .reason(r.getReason())
                .build();
    }
}