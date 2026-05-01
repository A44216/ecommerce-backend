package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.requests.SystemNotificationRequest;
import com.ecommerce.backend.dto.responses.SystemNotificationResponse;
import com.ecommerce.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
@CrossOrigin
public class AdminNotificationController {

    private final NotificationService notificationService;

    // Lấy lịch sử các thông báo hệ thống đã gửi
    @GetMapping
    public List<SystemNotificationResponse> getSystemNotifications() {
        return notificationService.getSystemNotifications();
    }

    // Admin gửi thông báo mới tới toàn bộ user
    @PostMapping("/broadcast")
    public void broadcastNotification(@RequestBody SystemNotificationRequest request) {
        notificationService.broadcastNotification(request.getTitle(), request.getBody(), request.getType());
    }
}
