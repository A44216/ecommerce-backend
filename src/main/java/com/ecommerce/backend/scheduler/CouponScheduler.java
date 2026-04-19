package com.ecommerce.backend.scheduler;

import com.ecommerce.backend.enums.CouponStatus;
import com.ecommerce.backend.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponScheduler {

    private final CouponRepository couponRepository;

    // Chạy mỗi giờ một lần (0 phút, 0 giây, mỗi giờ)
    // Bạn có thể đổi thành cron = "0 0 0 * * ?" để chạy lúc 0h sáng mỗi ngày
    // Hoặc fixedRate = 60000 để chạy mỗi phút (dùng cho test)
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void updateExpiredCoupons() {
        log.info("Starting scheduled task to update expired coupons...");
        
        int updatedCount = couponRepository.updateStatusForExpiredCoupons(
                CouponStatus.EXPIRED,
                LocalDateTime.now()
        );

        if (updatedCount > 0) {
            log.info("Successfully updated {} expired coupons to EXPIRED status.", updatedCount);
        } else {
            log.info("No expired coupons to update.");
        }
    }
}
