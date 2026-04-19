package com.ecommerce.backend.scheduler;

import com.ecommerce.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderScheduler {

    private final OrderRepository orderRepository;

    // Chạy mỗi phút 1 lần (0 giây, mỗi phút)
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void cancelUnpaidQROrders() {
        log.info("Starting scheduled task to cancel unpaid QR orders pending > 30 minutes...");
        
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
        int updatedCount = orderRepository.cancelUnpaidQROrders(threshold);

        if (updatedCount > 0) {
            log.info("Successfully cancelled {} unpaid QR orders.", updatedCount);
        } else {
            log.info("No unpaid QR orders to cancel.");
        }
    }
}
