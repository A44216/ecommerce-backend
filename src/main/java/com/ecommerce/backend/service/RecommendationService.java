package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.RecommendationRequest;
import com.ecommerce.backend.dto.responses.RecommendationResponse;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.Recommendation;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.enums.ProductStatus;
import com.ecommerce.backend.enums.RecommendationType;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.RecommendationRepository;
import com.ecommerce.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public RecommendationService(
            RecommendationRepository recommendationRepository,
            UserRepository userRepository,
            ProductRepository productRepository
    ) {
        this.recommendationRepository = recommendationRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    public List<RecommendationResponse> getAllRecommendations() {
        return recommendationRepository.findAll().stream().map(this::mapToDTO).toList();
    }

    public List<RecommendationResponse> getByUser(Integer userId) {
        return recommendationRepository.findByUserId(userId).stream().map(this::mapToDTO).toList();
    }

    public void deleteRecommendation(Integer id) {
        Recommendation rec = recommendationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation not found"));
        recommendationRepository.delete(rec);
    }

    // BẮT ĐẦU: FUZZY LOGIC & XAI ENGINE
    public RecommendationResponse createRecommendation(RecommendationRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

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
        double sScore = product.getSoldCount() != null ? (double) product.getSoldCount() / sMax : 0;

        double pScore = 0.5; // Default nếu Pmax = Pmin
        if (pMax.compareTo(pMin) > 0 && product.getPrice() != null) {
            pScore = product.getPrice().subtract(pMin)
                    .divide(pMax.subtract(pMin), 4, RoundingMode.HALF_UP).doubleValue();
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
        rules.add(new FuzzyRule("L1", "Deal cực hời: Sản phẩm có giá cực tốt so với các mặt hàng cùng loại, bán rất chạy và được đánh giá rất cao!", 0.95,
                Math.min(Math.min(muGoodR, muHighS), muCheapP)));

        rules.add(new FuzzyRule("L2", "Lựa chọn an toàn: Mức giá hợp lý đúng với mặt bằng chung, chất lượng đã được nhiều người dùng kiểm chứng.", 0.80,
                Math.min(Math.min(muGoodR, muMediumS), muFairP)));

        rules.add(new FuzzyRule("L3", "Thuộc phân khúc cao cấp: Chất lượng vượt trội nhưng có mức giá khá cao, phù hợp với nhu cầu chuyên biệt.", 0.60,
                Math.min(Math.min(muGoodR, muLowS), muExpensiveP)));

        rules.add(new FuzzyRule("L4", "Tuy có mức giá rẻ và lượt mua cao, nhưng sản phẩm nhận nhiều phản hồi tiêu cực. Hãy cân nhắc kỹ!", 0.40,
                Math.min(Math.min(muPoorR, muHighS), muCheapP)));

        rules.add(new FuzzyRule("L5", "Sản phẩm không được khuyến nghị: Mức giá cao nhưng chất lượng đánh giá rất thấp.", 0.15,
                Math.min(Math.min(muPoorR, muLowS), muExpensiveP)));

        rules.add(new FuzzyRule("L6", "Sản phẩm cao cấp được tin dùng: Dù mức giá cao nhưng chất lượng tuyệt vời và lượt bán khủng đã khẳng định giá trị sản phẩm.", 0.85,
                Math.min(Math.min(muGoodR, muHighS), muExpensiveP)));

        rules.add(new FuzzyRule("L7", "Lựa chọn phân khúc cao cấp: Sản phẩm có chất lượng tốt, phù hợp cho người dùng ưu tiên trải nghiệm hàng đầu.", 0.68,
                Math.min(Math.min(muGoodR, muMediumS), muExpensiveP)));

        rules.add(new FuzzyRule("L8", "Món hời mới: Sản phẩm có giá rất tốt và đánh giá cao, dù chưa có nhiều lượt bán nhưng rất đáng để trải nghiệm.", 0.75,
                Math.min(Math.min(muGoodR, muLowS), muCheapP)));

        rules.add(new FuzzyRule("L9", "Sản phẩm quốc dân: Mức giá hợp lý, lượt bán ổn định và nhận được sự tin tưởng từ đông đảo cộng đồng.", 0.80,
                Math.min(Math.min(muAvgR, muHighS), muFairP)));

        rules.add(new FuzzyRule("L10", "Lựa chọn tiết kiệm: Giá thành rẻ, chất lượng ở mức ổn định, phù hợp với các nhu cầu mua sắm cơ bản.", 0.62,
                Math.min(Math.min(muAvgR, muMediumS), muCheapP)));

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

        // --- ĐÃ SỬA: LOGIC UPSERT THAY VÌ LUÔN TẠO MỚI ---
        Recommendation recommendation = recommendationRepository
                .findByUserIdAndProductIdAndType(user.getId(), product.getId(), RecommendationType.FUZZY)
                .orElse(new Recommendation()); // Tìm DB xem có chưa, chưa có mới tạo đối tượng mới

        // Ghi đè (Cập nhật) các thông số
        recommendation.setUser(user);
        recommendation.setProduct(product);
        recommendation.setRatingScore(BigDecimal.valueOf(rScore));
        recommendation.setSoldScore(BigDecimal.valueOf(sScore));
        recommendation.setPriceScore(BigDecimal.valueOf(pScore));
        recommendation.setScore(BigDecimal.valueOf(finalScore));
        recommendation.setType(RecommendationType.FUZZY);
        recommendation.setReason(xaiReason);

        return mapToDTO(recommendationRepository.save(recommendation));
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

    // Mapper DTO (Giữ nguyên của bạn)
    private RecommendationResponse mapToDTO(Recommendation r) {
        String imageUrl = null;
        if (r.getProduct().getImages() != null && !r.getProduct().getImages().isEmpty()) {
            imageUrl = r.getProduct().getImages().getFirst().getImageUrl();
        }
        return RecommendationResponse.builder()
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
    public void generateAllFuzzyRecommendationsForUser(Integer userId) {
        // --- ĐÃ XÓA 2 LỆNH DELETE VÀ FLUSH Ở ĐÂY ---

        // Lấy tất cả sản phẩm đang kinh doanh và được duyệt (Status = APPROVED)
        List<Product> allProducts = productRepository.findByStatusAndIsDeletedFalse(ProductStatus.APPROVED);

        // Lặp qua từng sản phẩm để chấm điểm Fuzzy & XAI
        for (Product p : allProducts) {
            try {
                RecommendationRequest request = new RecommendationRequest();
                request.setUserId(userId);
                request.setProductId(p.getId());

                // Tính toán và lưu DB (Cơ chế Upsert trong hàm này sẽ tự lo việc Cập nhật)
                this.createRecommendation(request);
            } catch (Exception e) {
                log.error("Error creating fuzzy recommendation for User {} - Product {}: {}",
                        userId, p.getId(), e.getMessage());
            }
        }
    }
}