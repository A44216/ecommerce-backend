package com.ecommerce.backend.scheduler;

import com.ecommerce.backend.service.ProductEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEvaluationScheduler {

    private final ProductEvaluationService evaluationService;

    /**
     * Tự động cập nhật gợi ý Fuzzy Logic & XAI cho toàn bộ hệ thống
     * Chạy lúc 00:00 (nửa đêm) mỗi ngày
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void updateDailyFuzzyEvaluations() {
        log.info("Bắt đầu tiến trình cập nhật điểm AI Fuzzy Logic cho toàn bộ sản phẩm...");

        long startTime = System.currentTimeMillis();

        try {
            evaluationService.generateGlobalFuzzyEvaluations();

            long endTime = System.currentTimeMillis();
            log.info("Tiến trình cập nhật Fuzzy thành công! Tổng thời gian chạy: {} ms", (endTime - startTime));

        } catch (Exception e) {
            log.error("Có lỗi xảy ra trong quá trình chạy Cronjob Fuzzy: {}", e.getMessage(), e);
        }
    }
}