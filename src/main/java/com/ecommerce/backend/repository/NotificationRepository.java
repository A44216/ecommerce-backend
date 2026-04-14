package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    // Lấy danh sách thông báo của 1 user, sắp xếp mới nhất lên đầu
    List<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId);

    // Đếm số lượng thông báo CHƯA ĐỌC để hiển thị chấm đỏ trên App
    long countByUserIdAndIsReadFalse(Integer userId);
}