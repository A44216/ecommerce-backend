package com.ecommerce.backend.entity;

import com.ecommerce.backend.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Liên kết với bảng User (Ai là người nhận thông báo này)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String title; // Tiêu đề

    @Column(columnDefinition = "TEXT")
    private String body;  // Nội dung chi tiết

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50)
    private NotificationType type;

    // Lưu lại ID của Đơn hàng để khi user bấm vào thông báo, App biết mở đơn hàng nào lên
    private Integer relatedId;

    // Đánh dấu đã đọc hay chưa (Mặc định khi mới tạo là chưa đọc)
    private boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Tự động lấy giờ hiện tại khi lưu vào DB
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}