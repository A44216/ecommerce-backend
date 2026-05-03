package com.ecommerce.backend.enums;

public enum OrderStatus {
    PENDING("Chờ xác nhận"),
    CONFIRMED("Đang chuẩn bị hàng"),
    SHIPPING("Đang giao hàng"),
    COMPLETED("Đã giao thành công"),
    CANCELED("Đã hủy"),
    RETURN_REQUESTED("Yêu cầu trả hàng/hoàn tiền"),
    RETURNED("Đã hoàn tiền"),
    DISPUTED("Đang tranh chấp (Admin xử lý)");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}