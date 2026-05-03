package com.ecommerce.backend.scheduler;

import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecommendationScheduler {

    private final RecommendationService recommendationService;
    private final UserRepository userRepository;

    /**
     * Tự động cập nhật gợi ý Fuzzy Logic & XAI cho toàn bộ hệ thống
     * Chạy lúc 00:00 (nửa đêm) mỗi ngày
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void updateDailyRecommendations() {
        log.info("Starting daily Fuzzy Logic & XAI update job...");

        // 1. Lấy danh sách tất cả người dùng
        List<User> users = userRepository.findAll();

        // 2. Chấm điểm lại toàn bộ sản phẩm cho từng người dùng
        for (User user : users) {
            log.info("Generating recommendations for user: {}", user.getUsername());
            recommendationService.generateAllFuzzyRecommendationsForUser(user.getId());
        }

        log.info("Successfully updated recommendations for {} users.", users.size());
    }
}