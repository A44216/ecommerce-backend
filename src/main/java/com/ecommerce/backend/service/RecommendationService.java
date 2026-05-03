package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.responses.ProductBaseResponse;
import com.ecommerce.backend.dto.responses.RecommendationResponse;
import com.ecommerce.backend.entity.ProductEvaluation;
import com.ecommerce.backend.entity.Recommendation;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.repository.*;
import com.ecommerce.backend.util.SecurityUtils;
import com.ecommerce.backend.enums.ProductEvaluationType;
import com.ecommerce.backend.enums.UserStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final ProductEvaluationRepository evaluationRepository;
    private final OrderRepository orderRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public void generateRecommendationsForUser() {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null || currentUser.getStatus() != UserStatus.ACTIVE) return;

        Integer userId = currentUser.getId();
        recommendationRepository.deleteByUserId(userId);

        // Lấy danh mục yêu thích (đã có trong OrderRepository của bạn)
        Integer favoriteCatId = orderRepository.findFavoriteCategoryIdByUserId(userId).orElse(null);

        // Lấy Top sản phẩm từ Fuzzy Logic
        List<ProductEvaluation> topEvaluations = evaluationRepository.findTopProductsByType(
                ProductEvaluationType.FUZZY, PageRequest.of(0, 50));

        // Chuyển đổi sang danh sách Recommendation để saveAll một lần
        List<Recommendation> recommendations = topEvaluations.stream().map(eval -> {
            double finalScore = eval.getScore().doubleValue();
            String reason = eval.getReason();

            // Ưu tiên sở thích người dùng (Cộng 0.15 điểm)
            if (favoriteCatId != null && eval.getProduct().getCategory().getId().equals(favoriteCatId)) {
                finalScore = Math.min(1.0, finalScore + 0.15);
                reason = "Dựa trên sở thích của bạn: " + eval.getReason();
            }

            return Recommendation.builder()
                    .user(currentUser)
                    .product(eval.getProduct())
                    .score(BigDecimal.valueOf(finalScore))
                    .reason(reason)
                    .build();
        }).toList();

        recommendationRepository.saveAll(recommendations);
        log.info("Đã cập nhật {} gợi ý cho người dùng {}", recommendations.size(), userId);
    }

    public List<RecommendationResponse> getPersonalizedRecs(int limit) {
        Integer userId = securityUtils.getCurrentUserId();
        List<Recommendation> recs = recommendationRepository.findByUserIdOrderByScoreDesc(userId, PageRequest.of(0, limit));

        return recs.stream().map(rec -> {
            var p = rec.getProduct();

            // Mapping thông tin sản phẩm cơ bản
            ProductBaseResponse productBase = new ProductBaseResponse(
                    p.getId(),
                    p.getName(),
                    p.getProductCode(),
                    p.getPrice(),
                    p.getImages().isEmpty() ? null : p.getImages().getFirst().getImageUrl()
            );

            // Trả về DTO thay vì Entity
            return RecommendationResponse.builder()
                    .id(rec.getId())
                    .score(rec.getScore())
                    .reason(rec.getReason())
                    .product(productBase)
                    .build();
        }).toList();
    }
}