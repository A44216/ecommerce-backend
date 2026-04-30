package com.ecommerce.backend.service;
 
import java.util.HashMap;
import java.util.Map;

import com.ecommerce.backend.dto.responses.NotificationResponse;
import com.ecommerce.backend.entity.Notification;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.enums.NotificationType;
import com.ecommerce.backend.repository.NotificationRepository;
import com.ecommerce.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // 1. Lấy danh sách thông báo của User
    public List<NotificationResponse> getNotificationsForUser(Integer userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 2. Tạo thông báo mới (Hàm này sẽ được các Service khác gọi)
    @Transactional
    public void createNotification(Integer userId, String title, String body, NotificationType type, Integer relatedId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setType(type);
        notification.setRelatedId(relatedId);
        notification.setRead(false);

        notificationRepository.save(notification);
    }

    // 3. Đánh dấu đã đọc
    @Transactional
    public void markAsRead(Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    // 4. Đếm thông báo chưa đọc (cho chấm đỏ trên app)
    public long countUnread(Integer userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    // 5. Lấy tổng hợp số lượng chưa đọc theo từng loại
    public Map<String, Long> getUnreadCountsByType(Integer userId) {
        Map<String, Long> summary = new HashMap<>();
        for (NotificationType type : NotificationType.values()) {
            long count = notificationRepository.countByUserIdAndTypeAndIsReadFalse(userId, type);
            summary.put(type.name(), count);
        }
        return summary;
    }

    private NotificationResponse mapToResponse(Notification n) {
        NotificationResponse res = new NotificationResponse();
        res.setId(n.getId());
        res.setTitle(n.getTitle());
        res.setBody(n.getBody());
        res.setType(n.getType());
        res.setRelatedId(n.getRelatedId());
        res.setRead(n.isRead());
        res.setCreatedAt(n.getCreatedAt());
        return res;
    }
}