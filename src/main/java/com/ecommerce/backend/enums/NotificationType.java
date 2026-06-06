package com.ecommerce.backend.enums;

public enum NotificationType {
    ORDER,        // Thông báo về đơn hàng cho người mua
    SELLER_ORDER, // Thông báo về đơn hàng cho người bán
    PROMOTION,    // Thông báo khuyến mãi
    SYSTEM,       // Thông báo từ hệ thống
    LIVE,         // Live & Video
    AWARDS,       // Giải thưởng
    FOOD,         // Giao đồ ăn
    SHOP,         // Thông báo về Shop (Đăng ký, Duyệt shop...)
    PRODUCT,      // Thông báo về Sản phẩm (Duyệt sản phẩm, bị từ chối...)
    COMPLAINT     // Thông báo về Khiếu nại
}