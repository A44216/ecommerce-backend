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
    private final SecurityUtils securityUtils;

    @Transactional
    public void generateRecommendationsForUser() {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null || currentUser.getStatus() != UserStatus.ACTIVE) return;

        Integer userId = currentUser.getId();

        // 1. Lấy tất cả gợi ý hiện có của User này và đưa vào Map (Key là productId)
        Map<Integer, Recommendation> existingRecsMap = recommendationRepository.findAllByUserId(userId)
                .stream()
                .collect(Collectors.toMap(r -> r.getProduct().getId(), r -> r));

        // 2. Lấy danh mục yêu thích
        Integer favoriteCatId = orderRepository.findFavoriteCategoryIdByUserId(userId).orElse(null);

        // 3. Lấy Top sản phẩm từ Fuzzy Logic
        List<ProductEvaluation> topEvaluations = evaluationRepository.findTopProductsByType(
                ProductEvaluationType.FUZZY, PageRequest.of(0, 50));

        // 4. Xử lý logic Update hoặc Create
        List<Recommendation> toSave = topEvaluations.stream().map(eval -> {
            double finalScore = eval.getScore().doubleValue();
            String reason = eval.getReason();
            Integer productId = eval.getProduct().getId();

            if (favoriteCatId != null && eval.getProduct().getCategory().getId().equals(favoriteCatId)) {
                finalScore = Math.min(1.0, finalScore + 0.15);
                reason = "Dựa trên sở thích của bạn: " + eval.getReason();
            }

            // KIỂM TRA: Nếu đã tồn tại thì lấy bản ghi cũ ra update, nếu chưa thì tạo mới
            Recommendation rec = existingRecsMap.getOrDefault(productId, new Recommendation());

            if (rec.getId() == null) { // Tạo mới
                rec.setUser(currentUser);
                rec.setProduct(eval.getProduct());
            }

            rec.setScore(BigDecimal.valueOf(finalScore));
            rec.setReason(reason);

            // Đánh dấu bản ghi này vẫn còn trong Top 50 bằng cách xóa khỏi Map tạm
            existingRecsMap.remove(productId);

            return rec;
        }).toList();

        // 5. Lưu (Hibernate sẽ tự động INSERT cái mới và UPDATE cái cũ dựa trên ID)
        recommendationRepository.saveAll(toSave);

        // 6. XÓA các bản ghi cũ không còn nằm trong Top 50 nữa
        if (!existingRecsMap.isEmpty()) {
            recommendationRepository.deleteAll(existingRecsMap.values());
        }

        log.info("Đã cập nhật/tạo mới {} gợi ý cho người dùng {}", toSave.size(), userId);
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