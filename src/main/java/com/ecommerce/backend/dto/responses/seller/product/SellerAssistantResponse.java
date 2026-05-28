package com.ecommerce.backend.dto.responses.seller.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerAssistantResponse {
    private Integer productId;
    private BigDecimal score;        // Điểm đánh giá tổng hợp
    private BigDecimal priceScore;   // Điểm chuẩn hóa giá
    private BigDecimal soldScore;    // Điểm chuẩn hóa lượt bán
    private BigDecimal ratingScore;  // Điểm chuẩn hóa đánh giá
    private String analysis;         // Bản phân tích độ hấp dẫn (Reason)
    private String recommendation;   // Lời khuyên hành động cụ thể (Actionable Advice)
}
