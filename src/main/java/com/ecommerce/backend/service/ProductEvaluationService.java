package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.ProductEvaluationRequest;
import com.ecommerce.backend.dto.responses.ProductBaseResponse;
import com.ecommerce.backend.dto.responses.ProductEvaluationResponse;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.ProductEvaluation;
import com.ecommerce.backend.enums.ProductStatus;
import com.ecommerce.backend.enums.ProductEvaluationType;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.ProductEvaluationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    // Lấy danh sách sản phẩm Xu hướng hiển thị lên trang chủ
    public List<ProductEvaluationResponse> getTopTrendingDeals(int limit) {
        return evaluationRepository.findTopProductsByType(
                        ProductEvaluationType.TRENDING,
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

    public ProductEvaluationResponse evaluateProduct(ProductEvaluationRequest request) {
        // 1. Tìm sản phẩm
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // 2. Lấy dữ liệu ngữ cảnh (Context) trực tiếp từ Repository cho sản phẩm này
        Integer catId = product.getCategory().getId();
        int sMax = productRepository.findMaxSoldCountByCategoryId(catId) != null
                ? productRepository.findMaxSoldCountByCategoryId(catId) : 1;
        BigDecimal pMin = productRepository.findMinPriceByCategoryId(catId) != null
                ? productRepository.findMinPriceByCategoryId(catId) : BigDecimal.ZERO;
        BigDecimal pMax = productRepository.findMaxPriceByCategoryId(catId) != null
                ? productRepository.findMaxPriceByCategoryId(catId) : BigDecimal.ONE;

        // 3. Khởi tạo ngữ cảnh tạm thời
        CategoryContext ctx = new CategoryContext(sMax, pMin, pMax);

        // 4. Gọi hàm lõi tối ưu để thực hiện tính toán và lưu DB
        ProductEvaluation evaluation = evaluateProductWithContext(product, ctx);

        // 5. Trả về kết quả DTO
        return mapToDTO(evaluation);
    }

    private ProductEvaluation evaluateProductWithContext(Product product, CategoryContext ctx) {
        // BƯỚC 1: CHUẨN HÓA (Dùng trực tiếp dữ liệu từ ctx)
        double rScore = product.getRatingAvg() != null ? product.getRatingAvg().doubleValue() / 5.0 : 0;
        double sScore = product.getSoldCount() != null && ctx.sMax > 0 ? (double) product.getSoldCount() / ctx.sMax : 0;

        double pScore = 0.5;
        if (ctx.pMax != null && ctx.pMin != null && ctx.pMax.compareTo(ctx.pMin) > 0 && product.getPrice() != null) {
            pScore = product.getPrice().subtract(ctx.pMin)
                    .divide(ctx.pMax.subtract(ctx.pMin), 4, RoundingMode.HALF_UP).doubleValue();
        }

        // BƯỚC 2: MỜ HÓA (FUZZIFICATION)
        double muPoorR = fuzzyLeftShoulder(rScore, 0.0, 0.4);
        double muAvgR = fuzzyTriangle(rScore, 0.2, 0.5, 0.8);
        double muGoodR = fuzzyRightShoulder(rScore, 0.6, 1.0);

        double muLowS = fuzzyLeftShoulder(sScore, 0.0, 0.4);
        double muMediumS = fuzzyTriangle(sScore, 0.2, 0.5, 0.8);
        double muHighS = fuzzyRightShoulder(sScore, 0.6, 1.0);

        double muCheapP = fuzzyLeftShoulder(pScore, 0.0, 0.4);
        double muFairP = fuzzyTriangle(pScore, 0.2, 0.5, 0.8);
        double muExpensiveP = fuzzyRightShoulder(pScore, 0.6, 1.0);

        // BƯỚC 3 & 4: SUY LUẬN LUẬT MAMDANI VÀ TRÍCH XUẤT XAI
        List<FuzzyRule> rules = new ArrayList<>();

        // Nhóm luật chuyên biệt (L1-L11)
        rules.add(new FuzzyRule("L1", "Deal cực hời: Sản phẩm có giá cực tốt so với các mặt hàng cùng loại, bán rất chạy và được đánh giá rất cao!", 0.95, Math.min(Math.min(muGoodR, muHighS), muCheapP)));
        rules.add(new FuzzyRule("L2", "Lựa chọn an toàn: Mức giá hợp lý đúng với mặt bằng chung, chất lượng đã được nhiều người dùng kiểm chứng.", 0.80, Math.min(Math.min(muGoodR, muMediumS), muFairP)));
        rules.add(new FuzzyRule("L3", "Thuộc phân khúc cao cấp: Chất lượng vượt trội nhưng có mức giá khá cao, phù hợp với nhu cầu chuyên biệt.", 0.50, Math.min(Math.min(muGoodR, muLowS), muExpensiveP)));
        rules.add(new FuzzyRule("L4", "Tuy có mức giá rẻ và lượt mua cao, nhưng sản phẩm nhận nhiều phản hồi tiêu cực. Hãy cân nhắc kỹ!", 0.30, Math.min(Math.min(muPoorR, muHighS), muCheapP)));
        rules.add(new FuzzyRule("L5", "Sản phẩm không được khuyến nghị: Mức giá cao nhưng chất lượng đánh giá rất thấp.", 0.15, Math.min(Math.min(muPoorR, muLowS), muExpensiveP)));
        rules.add(new FuzzyRule("L6", "Sản phẩm cao cấp được tin dùng: Dù mức giá cao nhưng chất lượng tuyệt vời và lượt bán khủng đã khẳng định giá trị sản phẩm.", 0.80, Math.min(Math.min(muGoodR, muHighS), muExpensiveP)));
        rules.add(new FuzzyRule("L7", "Lựa chọn phân khúc cao cấp: Sản phẩm có chất lượng tốt, phù hợp cho người dùng ưu tiên trải nghiệm hàng đầu.", 0.68, Math.min(Math.min(muGoodR, muMediumS), muExpensiveP)));
        rules.add(new FuzzyRule("L8", "Món hời mới: Sản phẩm có giá rất tốt và đánh giá cao, dù chưa có nhiều lượt bán nhưng rất đáng để trải nghiệm.", 0.68, Math.min(Math.min(muGoodR, muLowS), muCheapP)));
        rules.add(new FuzzyRule("L9", "Sản phẩm quốc dân: Mức giá hợp lý, lượt bán ổn định và nhận được sự tin tưởng từ đông đảo cộng đồng.", 0.80, Math.min(Math.min(muAvgR, muHighS), muFairP)));
        rules.add(new FuzzyRule("L10", "Lựa chọn tiết kiệm: Giá thành rẻ, chất lượng ở mức ổn định, phù hợp với các nhu cầu mua sắm cơ bản.", 0.50, Math.min(Math.min(muAvgR, muMediumS), muCheapP)));
        rules.add(new FuzzyRule("L11", "Sản phẩm đắt khách: Giá siêu rẻ, bán rất chạy nhưng chất lượng chỉ ở mức trung bình. Phù hợp dùng tạm.", 0.68, Math.min(Math.min(muAvgR, muHighS), muCheapP)));

        // Nhóm luật bao quát (L12-L14)
        double muAny = 1.0;
        rules.add(new FuzzyRule("L12", "Sản phẩm nhận nhiều phản hồi chưa tốt từ người dùng, bạn nên cân nhắc kỹ trước khi quyết định mua hàng.", 0.30, Math.min(muPoorR, muAny)));
        rules.add(new FuzzyRule("L13", "Sản phẩm có chất lượng ở mức cơ bản, đáp ứng được các nhu cầu mua sắm và sử dụng phổ thông.", 0.50, Math.min(muAvgR, muAny)));
        rules.add(new FuzzyRule("L14", "Sản phẩm có chất lượng tốt, nhận được nhiều phản hồi tích cực và sự tin tưởng từ cộng đồng người dùng.", 0.68, Math.min(muGoodR, muAny)));

        // Chọn Luật chi phối
        FuzzyRule dominantRule = rules.getFirst();
        for (FuzzyRule rule : rules) {
            if (rule.firingStrength > dominantRule.firingStrength) dominantRule = rule;
        }

        // BƯỚC 5: GIẢI MỜ (DEFUZZIFICATION)
        double numerator = 0;
        double denominator = 0;
        for (FuzzyRule rule : rules) {
            numerator += rule.firingStrength * rule.centroidValue;
            denominator += rule.firingStrength;
        }

        double finalScore = denominator > 0 ? numerator / denominator : 0.5;
        String xaiReason = denominator > 0 ? dominantRule.reason : "Hệ thống đang phân tích thêm dữ liệu để đánh giá.";

        // LƯU KẾT QUẢ
        ProductEvaluation evaluation = evaluationRepository
                .findByProductIdAndType(product.getId(), ProductEvaluationType.FUZZY)
                .orElse(new ProductEvaluation());

        evaluation.setProduct(product);
        evaluation.setRatingScore(BigDecimal.valueOf(rScore));
        evaluation.setSoldScore(BigDecimal.valueOf(sScore));
        evaluation.setPriceScore(BigDecimal.valueOf(pScore));
        evaluation.setScore(BigDecimal.valueOf(finalScore));
        evaluation.setType(ProductEvaluationType.FUZZY);
        evaluation.setReason(xaiReason);

        return evaluationRepository.save(evaluation);
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
        var p = r.getProduct();

        // Tạo thông tin sản phẩm cơ bản
        ProductBaseResponse productBase = new ProductBaseResponse(
                p.getId(),
                p.getName(),
                p.getProductCode(),
                p.getPrice(),
                (p.getImages() != null && !p.getImages().isEmpty()) ? p.getImages().getFirst().getImageUrl() : null
        );

        return ProductEvaluationResponse.builder()
                .id(r.getId())
                .product(productBase)
                .score(r.getScore())
                .soldScore(r.getSoldScore())
                .ratingScore(r.getRatingScore())
                .priceScore(r.getPriceScore())
                .type(r.getType().name())
                .reason(r.getReason())
                .build();
    }

    public void generateGlobalFuzzyEvaluations() {
        List<Product> allProducts = productRepository.findByStatusAndIsDeletedFalse(ProductStatus.APPROVED);
        Map<Integer, CategoryContext> contextMap = new HashMap<>();

        log.info("Bắt đầu chấm điểm AI cho {} sản phẩm...", allProducts.size());

        for (Product p : allProducts) {
            try {
                Integer catId = p.getCategory().getId();

                // KIỂM TRA CACHE: Chỉ truy vấn DB nếu danh mục này chưa có dữ liệu Max/Min
                if (!contextMap.containsKey(catId)) {
                    int sMax = productRepository.findMaxSoldCountByCategoryId(catId) != null ? productRepository.findMaxSoldCountByCategoryId(catId) : 1;
                    BigDecimal pMin = productRepository.findMinPriceByCategoryId(catId) != null ? productRepository.findMinPriceByCategoryId(catId) : BigDecimal.ZERO;
                    BigDecimal pMax = productRepository.findMaxPriceByCategoryId(catId) != null ? productRepository.findMaxPriceByCategoryId(catId) : BigDecimal.ONE;

                    contextMap.put(catId, new CategoryContext(sMax, pMin, pMax));
                    log.info("==> Nạp Cache danh mục ID: {}", catId);
                }

                CategoryContext ctx = contextMap.get(catId);

                // Chấm điểm Fuzzy và Trending
                this.evaluateProductWithContext(p, ctx);
                this.evaluateTrendingProduct(p.getId());

            } catch (Exception e) {
                log.error("Lỗi tại ID {}: {}", p.getId(), e.getMessage());
            }
        }
        log.info("Hoàn tất tiến trình chấm điểm.");
    }

    // Chấm điểm Trending cho sản phẩm
    public ProductEvaluationResponse evaluateTrendingProduct(Integer productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        java.time.LocalDateTime oneMonthAgo = LocalDateTime.now().minusDays(30);
        int salesInMonth = productRepository.countRecentSales(productId, oneMonthAgo);

        double salesFactor = Math.min((double) salesInMonth / 100.0, 1.0);
        double ratingFactor = product.getRatingAvg() != null ? product.getRatingAvg().doubleValue() / 5.0 : 0.5;

        // SỬA: Nếu sales = 0 thì điểm gần như bằng 0
        double trendingScore = (salesInMonth == 0) ? ratingFactor * 0.1 : (salesFactor * 0.8) + (ratingFactor * 0.2);

        String xaiReason = (salesInMonth >= 50) ? "Xu hướng tháng: Đang cực hot với hơn " + salesInMonth + " lượt bán." :
                (salesInMonth >= 10) ? "Đang tăng trưởng: Lượt mua tăng ổn định trong tháng." :
                        (salesInMonth > 0) ? "Tiềm năng: Đang có lượt bán và phản hồi tốt." : "Sản phẩm mới: Đang chờ lượt trải nghiệm từ cộng đồng.";

        ProductEvaluation evaluation = evaluationRepository.findByProductIdAndType(product.getId(), ProductEvaluationType.TRENDING).orElse(new ProductEvaluation());
        evaluation.setProduct(product);
        evaluation.setScore(BigDecimal.valueOf(trendingScore));
        evaluation.setType(ProductEvaluationType.TRENDING);
        evaluation.setReason(xaiReason);
        evaluation.setSoldScore(BigDecimal.valueOf(salesFactor));
        evaluation.setRatingScore(BigDecimal.valueOf(ratingFactor));
        evaluation.setPriceScore(BigDecimal.ZERO);

        return mapToDTO(evaluationRepository.save(evaluation));
    }

    private static class CategoryContext {
        int sMax;
        BigDecimal pMin;
        BigDecimal pMax;

        public CategoryContext(int sMax, BigDecimal pMin, BigDecimal pMax) {
            this.sMax = sMax;
            this.pMin = pMin;
            this.pMax = pMax;
        }
    }

}