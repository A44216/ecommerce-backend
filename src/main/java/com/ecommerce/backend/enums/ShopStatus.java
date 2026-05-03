package com.ecommerce.backend.enums;

public enum ShopStatus {
    PENDING,    // chờ duyệt
    APPROVED,   // đã duyệt
    REJECTED,   // bị từ chối
    BLOCKED,    // bị admin khóa
    CANCELED    // người dùng hủy yêu cầu
}