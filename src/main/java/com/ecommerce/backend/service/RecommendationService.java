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
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final ProductEvaluationRepository evaluationRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public void generateRecommendationsForUser() {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) return;
        generateRecommendationsForUser(currentUser);
    }

    public void generateRecommendationsForAllUsers() {
        List<User> activeUsers = userRepository.findActiveUsers();
        log.info("Bắt đầu tạo recommendations cho {} users...", activeUsers.size());
        int success = 0, error = 0;
        for (User user : activeUsers) {
            try {
                generateRecommendationsForUser(user);
                success++;
            } catch (Exception e) {
                error++;
                log.error("Lỗi user {}: {}", user.getId(), e.getMessage());
            }
        }
        log.info("Hoàn thành: {} thành công, {} lỗi", success, error);
    }

    @Transactional
    public void generateRecommendationsForUser(User user) {
        if (user == null || user.getStatus() != UserStatus.ACTIVE) return;
        Integer userId = user.getId();

        Map<Integer, Recommendation> existingRecsMap = recommendationRepository.findAllByUserId(userId)
                .stream()
                .collect(Collectors.toMap(r -> r.getProduct().getId(), r -> r));

        Integer favoriteCatId = orderRepository.findFavoriteCategoryIdByUserId(userId).orElse(null);

        List<ProductEvaluation> topEvaluations = evaluationRepository.findTopProductsByType(
                ProductEvaluationType.FUZZY, PageRequest.of(0, 50));

        List<Recommendation> toSave = topEvaluations.stream().map(eval -> {
            double finalScore = eval.getScore().doubleValue();
            String reason = eval.getReason();
            Integer productId = eval.getProduct().getId();

            if (eval.getProduct().getCategory().getId().equals(favoriteCatId)) {
                finalScore = Math.min(1.0, finalScore + 0.15);
                reason = "Dựa trên sở thích của bạn: " + eval.getReason();
            }

            Recommendation rec = existingRecsMap.getOrDefault(productId, new Recommendation());

            if (rec.getId() == null) {
                rec.setUser(user);
                rec.setProduct(eval.getProduct());
            }

            rec.setScore(BigDecimal.valueOf(finalScore));
            rec.setReason(reason);
            existingRecsMap.remove(productId);

            return rec;
        }).toList();

        recommendationRepository.saveAll(toSave);

        if (!existingRecsMap.isEmpty()) {
            recommendationRepository.deleteAll(existingRecsMap.values());
        }

        log.info("Đã cập nhật {} gợi ý cho user {}", toSave.size(), userId);
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