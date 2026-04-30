package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.responses.NotificationResponse;
import com.ecommerce.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // Lấy tất cả thông báo của tôi
    @GetMapping("/user/{userId}")
    public List<NotificationResponse> getMyNotifications(@PathVariable Integer userId) {
        return notificationService.getNotificationsForUser(userId);
    }

    // Lấy số lượng chưa đọc tổng quát
    @GetMapping("/user/{userId}/unread-count")
    public Map<String, Long> getUnreadCount(@PathVariable Integer userId) {
        return Collections.singletonMap("unreadCount", notificationService.countUnread(userId));
    }

    // Lấy số lượng chưa đọc chi tiết theo loại
    @GetMapping("/user/{userId}/summary")
    public Map<String, Long> getNotificationSummary(@PathVariable Integer userId) {
        return notificationService.getUnreadCountsByType(userId);
    }

    // Đánh dấu 1 thông báo là đã đọc
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Integer id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
}