package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.responses.RecommendationResponse;
import com.ecommerce.backend.entity.Recommendation;
import com.ecommerce.backend.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    // Lấy danh sách gợi ý cá nhân hóa cho người dùng hiện tại
    @GetMapping("/my-recs")
    public List<RecommendationResponse> getMyRecs(
            @RequestParam(defaultValue = "10") int limit) {
        // Bây giờ Service trả về DTO nên sẽ không còn bị lặp JSON
        return recommendationService.getPersonalizedRecs(limit);
    }

    // Yêu cầu hệ thống tính toán lại sở thích
    @PostMapping("/refresh")
    public void refreshRecs() {
        recommendationService.generateRecommendationsForUser();
    }

    // [DÀNH CHO TEST] Kích hoạt tạo lại gợi ý cho TẤT CẢ user
    @PostMapping("/refresh-all")
    public String refreshAllRecs() {
        recommendationService.generateRecommendationsForAllUsers();
        return "Đã kích hoạt tạo Recommendations cho tất cả user thành công!";
    }
}