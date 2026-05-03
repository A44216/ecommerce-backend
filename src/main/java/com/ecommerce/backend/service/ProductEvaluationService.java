package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.ProductEvaluationRequest;
import com.ecommerce.backend.dto.responses.ProductEvaluationResponse;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.ProductEvaluation;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.enums.ProductStatus;
import com.ecommerce.backend.enums.ProductEvaluationType;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.ProductEvaluationRepository;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.util.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductEvaluationService {

    private final ProductEvaluationRepository evaluationRepository;
    private final ProductRepository productRepository;

    public ProductEvaluationService(
            ProductEvaluationRepository evaluationRepository,
            ProductRepository productRepository
    ) {
        this.evaluationRepository = evaluationRepository;
        this.productRepository = productRepository;
    }

    // Lấy danh sách hiển thị Trang chủ (Ví dụ: Top 20 Deal Hời nhất)
    public List<ProductEvaluationResponse> getTopFuzzyDeals(int limit) {
        return evaluationRepository.findTopProductsByType(
                        ProductEvaluationType.FUZZY,
                        PageRequest.of(0, limit)
                ).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public void deleteEvaluation(Integer id) {
        ProductEvaluation eval = evaluationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found"));
        evaluationRepository.delete(eval);
    }

    // BẮT ĐẦU: FUZZY LOGIC & XAI ENGINE
    public ProductEvaluationResponse evaluateProduct(ProductEvaluationRequest request) {

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Integer catId = product.getCategory().getId();

        // Lấy dữ liệu ngữ cảnh theo danh mục
        int sMax = productRepository.findMaxSoldCountByCategoryId(catId) != null
                ? productRepository.findMaxSoldCountByCategoryId(catId) : 1;
        BigDecimal pMin = productRepository.findMinPriceByCategoryId(catId) != null
                ? productRepository.findMinPriceByCategoryId(catId) : BigDecimal.ZERO;
        BigDecimal pMax = productRepository.findMaxPriceByCategoryId(catId) != null
                ? productRepository.findMaxPriceByCategoryId(catId) : BigDecimal.ONE;

        // BƯỚC 1: CHUẨN HÓA ĐẦU VÀO (NORMALIZATION)
        double rScore = product.getRatingAvg() != null ? product.getRatingAvg().doubleValue() / 5.0 : 0;
        double sScore = product.getSoldCount() != null && sMax > 0 ? (double) product.getSoldCount() / sMax : 0;

        double pScore = 0.5; // Mặc định là mức giá "Hợp lý" nếu không có khoảng giá
        if (pMax != null && pMin != null && pMax.compareTo(pMin) > 0 && product.getPrice() != null) {
            pScore = product.getPrice().subtract(pMin)
                    .divide(pMax.subtract(pMin), 4, RoundingMode.HALF_UP).doubleValue();
        } else {
            // Nếu pMax == pMin (chỉ có 1 sản phẩm hoặc các sản phẩm bằng giá nhau), gán mặc định 0.5
            pScore = 0.5;
        }

        // BƯỚC 2: MỜ HÓA (FUZZIFICATION) - Tính độ kích hoạt (μ)
        // Rating (R)
        double muPoorR = fuzzyLeftShoulder(rScore, 0.0, 0.4);
        double muAvgR = fuzzyTriangle(rScore, 0.2, 0.5, 0.8);
        double muGoodR = fuzzyRightShoulder(rScore, 0.6, 1.0);

        // Sold (S)
        double muLowS = fuzzyLeftShoulder(sScore, 0.0, 0.4);
        double muMediumS = fuzzyTriangle(sScore, 0.2, 0.5, 0.8);
        double muHighS = fuzzyRightShoulder(sScore, 0.6, 1.0);

        // Price (P)
        double muCheapP = fuzzyLeftShoulder(pScore, 0.0, 0.4); // Giá càng gần 0 càng rẻ
        double muFairP = fuzzyTriangle(pScore, 0.2, 0.5, 0.8);
        double muExpensiveP = fuzzyRightShoulder(pScore, 0.6, 1.0); // Giá gần 1 là đắt

        // BƯỚC 3 & 4: SUY LUẬN LUẬT MAMDANI VÀ TRÍCH XUẤT XAI (CONFLICT RESOLUTION)
        List<FuzzyRule> rules = new ArrayList<>();

        // Bảng 2.1 Ma trận luật XAI cốt lõi
        // NHÓM LUẬT CHUYÊN BIỆT (L1 - L11)
        // L1: Deal hời (Giá rẻ, Bán chạy, Đánh giá cao)
        rules.add(new FuzzyRule("L1", "Deal cực hời: Sản phẩm có giá cực tốt so với các mặt hàng cùng loại, bán rất chạy và được đánh giá rất cao!", 0.95,
                Math.min(Math.min(muGoodR, muHighS), muCheapP)));

        // L2: Lựa chọn an toàn (Giá hợp lý, Bán vừa, Đánh giá cao)
        rules.add(new FuzzyRule("L2", "Lựa chọn an toàn: Mức giá hợp lý đúng với mặt bằng chung, chất lượng đã được nhiều người dùng kiểm chứng.", 0.80,
                Math.min(Math.min(muGoodR, muMediumS), muFairP)));

        // L3: Hàng cao cấp kén khách (Giá đắt, Bán ít, Đánh giá cao)
        rules.add(new FuzzyRule("L3", "Thuộc phân khúc cao cấp: Chất lượng vượt trội nhưng có mức giá khá cao, phù hợp với nhu cầu chuyên biệt.", 0.50,
                Math.min(Math.min(muGoodR, muLowS), muExpensiveP)));

        // L4: Cảnh báo chất lượng (Giá rẻ, Bán chạy nhưng Đánh giá tệ)
        rules.add(new FuzzyRule("L4", "Tuy có mức giá rẻ và lượt mua cao, nhưng sản phẩm nhận nhiều phản hồi tiêu cực. Hãy cân nhắc kỹ!", 0.30,
                Math.min(Math.min(muPoorR, muHighS), muCheapP)));

        // L5: Tránh xa (Giá đắt, Bán ít, Đánh giá tệ)
        rules.add(new FuzzyRule("L5", "Sản phẩm không được khuyến nghị: Mức giá cao nhưng chất lượng đánh giá rất thấp.", 0.15,
                Math.min(Math.min(muPoorR, muLowS), muExpensiveP)));

        // L6: Cao cấp tin dùng (Giá đắt, Bán chạy, Đánh giá cao)
        rules.add(new FuzzyRule("L6", "Sản phẩm cao cấp được tin dùng: Dù mức giá cao nhưng chất lượng tuyệt vời và lượt bán khủng đã khẳng định giá trị sản phẩm.", 0.80,
                Math.min(Math.min(muGoodR, muHighS), muExpensiveP)));

        // L7: Phân khúc cao cấp (Giá đắt, Bán vừa, Đánh giá cao)
        rules.add(new FuzzyRule("L7", "Lựa chọn phân khúc cao cấp: Sản phẩm có chất lượng tốt, phù hợp cho người dùng ưu tiên trải nghiệm hàng đầu.", 0.68,
                Math.min(Math.min(muGoodR, muMediumS), muExpensiveP)));

        // L8: Món hời mới (Giá rẻ, Bán ít, Đánh giá cao)
        rules.add(new FuzzyRule("L8", "Món hời mới: Sản phẩm có giá rất tốt và đánh giá cao, dù chưa có nhiều lượt bán nhưng rất đáng để trải nghiệm.", 0.68,
                Math.min(Math.min(muGoodR, muLowS), muCheapP)));

        // L9: Sản phẩm quốc dân (Giá hợp lý, Bán chạy, Đánh giá trung bình)
        rules.add(new FuzzyRule("L9", "Sản phẩm quốc dân: Mức giá hợp lý, lượt bán ổn định và nhận được sự tin tưởng từ đông đảo cộng đồng.", 0.80,
                Math.min(Math.min(muAvgR, muHighS), muFairP)));

        // L10: Lựa chọn tiết kiệm (Giá rẻ, Bán vừa, Đánh giá trung bình)
        rules.add(new FuzzyRule("L10", "Lựa chọn tiết kiệm: Giá thành rẻ, chất lượng ở mức ổn định, phù hợp với các nhu cầu mua sắm cơ bản.", 0.50,
                Math.min(Math.min(muAvgR, muMediumS), muCheapP)));

        // L11: Sản phẩm đắt khách (Giá rẻ, Bán chạy, Đánh giá trung bình)
        rules.add(new FuzzyRule("L11", "Sản phẩm đắt khách: Giá siêu rẻ, bán rất chạy nhưng chất lượng chỉ ở mức trung bình. Phù hợp dùng tạm.", 0.68,
                Math.min(Math.min(muAvgR, muHighS), muCheapP)));

        // NHÓM LUẬT BAO QUÁT (Bảo vệ hệ thống khi không khớp các luật trên)
        double muAny = 1.0;
        // L12: Mặc định dựa trên Rating thấp
        rules.add(new FuzzyRule("L12", "Sản phẩm nhận nhiều phản hồi chưa tốt từ người dùng, bạn nên cân nhắc kỹ trước khi quyết định mua hàng.", 0.30,
                Math.min(Math.min(muPoorR, muAny), muAny)));

        // L13: Mặc định dựa trên Rating trung bình
        rules.add(new FuzzyRule("L13", "Sản phẩm có chất lượng ở mức cơ bản, đáp ứng được các nhu cầu mua sắm và sử dụng phổ thông.", 0.50,
                Math.min(Math.min(muAvgR, muAny), muAny)));

        // L14: Mặc định dựa trên Rating tốt
        rules.add(new FuzzyRule("L14", "Sản phẩm có chất lượng tốt, nhận được nhiều phản hồi tích cực và sự tin tưởng từ cộng đồng người dùng.", 0.68,
                Math.min(Math.min(muGoodR, muAny), muAny)));

        // Chọn Luật chi phối (Dominant Rule) để làm lý do giải thích
        FuzzyRule dominantRule = rules.getFirst();
        for (FuzzyRule rule : rules) {
            if (rule.firingStrength > dominantRule.firingStrength) {
                dominantRule = rule;
            }
        }

        // BƯỚC 5: GIẢI MỜ (DEFUZZIFICATION) BẰNG PHƯƠNG PHÁP TRỌNG TÂM (CENTROID)
        double numerator = 0;
        double denominator = 0;
        for (FuzzyRule rule : rules) {
            numerator += rule.firingStrength * rule.centroidValue;
            denominator += rule.firingStrength;
        }

        // Tránh chia cho 0 nếu không có luật nào kích hoạt
        double finalScore = denominator > 0 ? numerator / denominator : 0.5;

        // Lưu câu văn tự nhiên làm XAI
        String xaiReason = denominator > 0 ? dominantRule.reason : "Chưa đủ dữ liệu để đánh giá sản phẩm này.";

        // Upsert bằng Product ID và Type
        ProductEvaluation evaluation = evaluationRepository
                .findByProductIdAndType(product.getId(), ProductEvaluationType.FUZZY)
                .orElse(new ProductEvaluation());

        // Ghi đè (Cập nhật) các thông số
        evaluation.setProduct(product);
        evaluation.setRatingScore(BigDecimal.valueOf(rScore));
        evaluation.setSoldScore(BigDecimal.valueOf(sScore));
        evaluation.setPriceScore(BigDecimal.valueOf(pScore));
        evaluation.setScore(BigDecimal.valueOf(finalScore));
        evaluation.setType(ProductEvaluationType.FUZZY);
        evaluation.setReason(xaiReason);

        return mapToDTO(evaluationRepository.save(evaluation));
    }

    // HÀM TOÁN HỌC LIÊN THUỘC (MEMBERSHIP FUNCTIONS)
    private double fuzzyLeftShoulder(double x, double a, double b) {
        if (x <= a) return 1.0;
        if (x >= b) return 0.0;
        return (b - x) / (b - a);
    }

    private double fuzzyTriangle(double x, double a, double b, double c) {
        if (x <= a || x >= c) return 0.0;
        if (x == b) return 1.0;
        if (x < b) return (x - a) / (b - a);
        return (c - x) / (c - b);
    }

    private double fuzzyRightShoulder(double x, double a, double b) {
        if (x <= a) return 0.0;
        if (x >= b) return 1.0;
        return (x - a) / (b - a);
    }

    // Class nội bộ hỗ trợ tính toán luật XAI
    private static class FuzzyRule {
        String code;
        String reason;
        double centroidValue; // Điểm trọng tâm của tập mờ đầu ra (ci)
        double firingStrength; // Mức độ kích hoạt của luật (α)

        public FuzzyRule(String code, String reason, double centroidValue, double firingStrength) {
            this.code = code;
            this.reason = reason;
            this.centroidValue = centroidValue;
            this.firingStrength = firingStrength;
        }
    }

    // Mapper DTO
    private ProductEvaluationResponse mapToDTO(ProductEvaluation r) {
        String imageUrl = null;
        if (r.getProduct().getImages() != null && !r.getProduct().getImages().isEmpty()) {
            imageUrl = r.getProduct().getImages().getFirst().getImageUrl();
        }
        return ProductEvaluationResponse.builder()
                .productId(r.getProduct().getId())
                .productName(r.getProduct().getName())
                .imageUrl(imageUrl)
                .price(r.getProduct().getPrice())
                .score(r.getScore())
                .soldScore(r.getSoldScore())
                .ratingScore(r.getRatingScore())
                .priceScore(r.getPriceScore())
                .type(r.getType().name())
                .reason(r.getReason())
                .build();
    }

    @Transactional
    public void generateGlobalFuzzyEvaluations() {
        List<Product> allProducts = productRepository.findByStatusAndIsDeletedFalse(ProductStatus.APPROVED);

        for (Product p : allProducts) {
            try {
                ProductEvaluationRequest request = new ProductEvaluationRequest();
                request.setProductId(p.getId());

                // Không cần truyền user nữa
                this.evaluateProduct(request);
            } catch (Exception e) {
                log.error("Error creating fuzzy evaluation for Product {}: {}", p.getId(), e.getMessage());
            }
        }
    }
}