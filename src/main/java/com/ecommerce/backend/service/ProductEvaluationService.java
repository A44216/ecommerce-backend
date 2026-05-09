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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
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

    @Transactional
    public void deleteEvaluation(Integer id) {
        ProductEvaluation eval = evaluationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found"));
        evaluationRepository.delete(eval);
    }

    @Transactional
    public ProductEvaluationResponse evaluateProduct(ProductEvaluationRequest request) {
        // 1. Tìm sản phẩm
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // 2. Lấy dữ liệu ngữ cảnh (Context)
        Integer catId = product.getCategory().getId();

        // Batch query để lấy context (sMax, pMin, pMax) cho category
        List<Object[]> contexts = productRepository.findAllCategoryContexts();
        CategoryContext ctx = new CategoryContext(1, BigDecimal.ZERO, BigDecimal.ONE);
        for (Object[] row : contexts) {
            if (row[0].equals(catId)) {
                Integer sMax = ((Number) row[1]).intValue();
                BigDecimal pMin = (BigDecimal) row[2];
                BigDecimal pMax = (BigDecimal) row[3];
                ctx = new CategoryContext(sMax > 0 ? sMax : 1, pMin != null ? pMin : BigDecimal.ZERO, pMax != null ? pMax : BigDecimal.ONE);
                break;
            }
        }

        // 3. Lấy total sales cho sản phẩm này (dùng cho Fuzzy - Deal Hời)
        int totalSales = product.getSoldCount() != null ? product.getSoldCount() : 0;

        // 4. Gọi hàm lõi để thực hiện tính toán và lưu DB
        ProductEvaluation evaluation = evaluateProductWithContext(product, ctx, totalSales);

        // 5. Trả về kết quả DTO
        return mapToDTO(evaluation);
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

    // Tối ưu: dùng sales count đã cache sẵn thay vì query lại từng sản phẩm
    @Transactional
    public ProductEvaluationResponse evaluateTrendingProductWithCache(Integer productId, int salesInMonth) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        double salesFactor = Math.min((double) salesInMonth / 100.0, 1.0);
        
        int ratingCount = product.getRatingCount() != null ? product.getRatingCount() : 0;
        double ratingFactor = 0.5;
        if (ratingCount > 0) {
            ratingFactor = product.getRatingAvg() != null ? product.getRatingAvg().doubleValue() / 5.0 : 0.5;
        }

        long daysSinceCreation = product.getCreatedAt() != null ? java.time.temporal.ChronoUnit.DAYS.between(product.getCreatedAt(), LocalDateTime.now()) : 30;
        boolean isNewArrival = daysSinceCreation <= 14;

        double trendingScore;
        String xaiReason;

        if (salesInMonth == 0) {
            if (isNewArrival) {
                trendingScore = 0.5 + (ratingFactor * 0.2);
                xaiReason = "Sản phẩm mới ra mắt: Đang thu hút sự chú ý, hãy là người đầu tiên trải nghiệm!";
            } else {
                trendingScore = ratingFactor * 0.1;
                xaiReason = "Chưa có lượt mua gần đây: Sản phẩm đang chờ bạn khám phá và trải nghiệm.";
            }
        } else {
            trendingScore = (salesFactor * 0.8) + (ratingFactor * 0.2);
            if (isNewArrival) {
                trendingScore = Math.min(trendingScore + 0.15, 1.0);
            }
            xaiReason = (salesInMonth >= 50) ? "Xu hướng tháng: Đang cực hot với hơn " + salesInMonth + " lượt bán." :
                    (salesInMonth >= 10) ? "Đang tăng trưởng: Lượt mua tăng ổn định trong tháng." :
                            "Tiềm năng: Đang có lượt bán và phản hồi tốt.";
        }

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

    @Transactional
    public void generateGlobalFuzzyEvaluations() {
        List<Product> allProducts = productRepository.findByStatusAndIsDeletedFalse(ProductStatus.APPROVED);

        // Tối ưu 1: Load tất cả category context trong 1 query
        Map<Integer, CategoryContext> contextMap = new ConcurrentHashMap<>();
        List<Object[]> categoryContexts = productRepository.findAllCategoryContexts();
        for (Object[] row : categoryContexts) {
            Integer catId = (Integer) row[0];
            Integer sMax = ((Number) row[1]).intValue();
            BigDecimal pMin = (BigDecimal) row[2];
            BigDecimal pMax = (BigDecimal) row[3];
            contextMap.put(catId, new CategoryContext(sMax, pMin, pMax));
        }

        List<Integer> productIds = allProducts.stream().map(Product::getId).toList();

        // Tối ưu 3: Batch load tất cả FUZZY evaluations trong 1 query
        Map<Integer, ProductEvaluation> fuzzyEvalMap = new ConcurrentHashMap<>();
        if (!productIds.isEmpty()) {
            List<ProductEvaluation> fuzzyEvals = evaluationRepository.findAllByProductIdsAndType(productIds, ProductEvaluationType.FUZZY);
            for (ProductEvaluation eval : fuzzyEvals) {
                fuzzyEvalMap.put(eval.getProduct().getId(), eval);
            }
        }

        log.info("Bắt đầu chấm điểm AI Fuzzy cho {} sản phẩm...", allProducts.size());

        for (Product p : allProducts) {
            try {
                Integer catId = p.getCategory().getId();
                CategoryContext ctx = contextMap.get(catId);
                if (ctx == null) {
                    ctx = new CategoryContext(1, BigDecimal.ZERO, BigDecimal.ONE);
                }

                int totalSales = p.getSoldCount() != null ? p.getSoldCount() : 0;

                // Chấm điểm Fuzzy với dữ liệu all-time (dùng map thay vì query)
                this.evaluateFuzzyWithMap(p, ctx, totalSales, fuzzyEvalMap);

            } catch (Exception e) {
                log.error("Lỗi tại ID {}: {}", p.getId(), e.getMessage());
            }
        }
        log.info("Hoàn tất tiến trình chấm điểm Fuzzy.");
    }

    @Transactional
    public void generateGlobalTrendingEvaluations() {
        List<Product> allProducts = productRepository.findByStatusAndIsDeletedFalse(ProductStatus.APPROVED);
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusDays(30);

        // Tối ưu 1: Load tất cả category context trong 1 query (Cần cho pScore của Xu Hướng)
        Map<Integer, CategoryContext> contextMap = new ConcurrentHashMap<>();
        List<Object[]> categoryContexts = productRepository.findAllCategoryContexts();
        for (Object[] row : categoryContexts) {
            Integer catId = (Integer) row[0];
            Integer sMax = ((Number) row[1]).intValue();
            BigDecimal pMin = (BigDecimal) row[2];
            BigDecimal pMax = (BigDecimal) row[3];
            contextMap.put(catId, new CategoryContext(sMax, pMin, pMax));
        }

        // Tối ưu 2: Batch load sales count trong 1 query
        List<Integer> productIds = allProducts.stream().map(Product::getId).toList();
        Map<Integer, Integer> salesMap = new ConcurrentHashMap<>();
        if (!productIds.isEmpty()) {
            List<Object[]> salesResults = productRepository.countRecentSalesBatch(productIds, oneMonthAgo);
            for (Object[] row : salesResults) {
                salesMap.put((Integer) row[0], ((Number) row[1]).intValue());
            }
        }

        // Tối ưu 4: Batch load tất cả TRENDING evaluations trong 1 query
        Map<Integer, ProductEvaluation> trendingEvalMap = new ConcurrentHashMap<>();
        if (!productIds.isEmpty()) {
            List<ProductEvaluation> trendingEvals = evaluationRepository.findAllByProductIdsAndType(productIds, ProductEvaluationType.TRENDING);
            for (ProductEvaluation eval : trendingEvals) {
                trendingEvalMap.put(eval.getProduct().getId(), eval);
            }
        }

        log.info("Bắt đầu chấm điểm AI Trending cho {} sản phẩm...", allProducts.size());

        for (Product p : allProducts) {
            try {
                Integer catId = p.getCategory().getId();
                CategoryContext ctx = contextMap.get(catId);
                if (ctx == null) {
                    ctx = new CategoryContext(1, BigDecimal.ZERO, BigDecimal.ONE);
                }

                double pScore = 0.5;
                if (ctx.pMax != null && ctx.pMin != null && ctx.pMax.compareTo(ctx.pMin) > 0 && p.getPrice() != null) {
                    pScore = p.getPrice().subtract(ctx.pMin)
                            .divide(ctx.pMax.subtract(ctx.pMin), 4, RoundingMode.HALF_UP).doubleValue();
                }

                int recentSales = salesMap.getOrDefault(p.getId(), 0);

                // Chấm điểm Trending với sales 30 ngày đã load sẵn (dùng map thay vì query)
                this.evaluateTrendingWithMap(p, recentSales, pScore, trendingEvalMap);

            } catch (Exception e) {
                log.error("Lỗi tại ID {}: {}", p.getId(), e.getMessage());
            }
        }
        log.info("Hoàn tất tiến trình chấm điểm Trending.");
    }

    private ProductEvaluation evaluateProductWithContext(Product product, CategoryContext ctx, int totalSales) {
        double pScore = 0.5;
        if (ctx.pMax != null && ctx.pMin != null && ctx.pMax.compareTo(ctx.pMin) > 0 && product.getPrice() != null) {
            pScore = product.getPrice().subtract(ctx.pMin)
                    .divide(ctx.pMax.subtract(ctx.pMin), 4, RoundingMode.HALF_UP).doubleValue();
        }

        int ratingCount = product.getRatingCount() != null ? product.getRatingCount() : 0;
        
        if (ratingCount == 0 && totalSales == 0) {
            long daysSinceCreation = product.getCreatedAt() != null ? java.time.temporal.ChronoUnit.DAYS.between(product.getCreatedAt(), LocalDateTime.now()) : 30;
            boolean isNewArrival = daysSinceCreation <= 14;
            
            String xaiReason = isNewArrival ? 
                "Sản phẩm mới ra mắt: Đang chờ những đánh giá đầu tiên từ cộng đồng người dùng." : 
                "Chưa có đủ dữ liệu đánh giá: Sản phẩm đang chờ được khám phá và trải nghiệm.";
                
            ProductEvaluation evaluation = evaluationRepository
                    .findByProductIdAndType(product.getId(), ProductEvaluationType.FUZZY)
                    .orElse(new ProductEvaluation());

            evaluation.setProduct(product);
            evaluation.setRatingScore(BigDecimal.valueOf(0.5));
            evaluation.setSoldScore(BigDecimal.ZERO);
            evaluation.setPriceScore(BigDecimal.valueOf(pScore));
            evaluation.setScore(BigDecimal.valueOf(0.5));
            evaluation.setType(ProductEvaluationType.FUZZY);
            evaluation.setReason(xaiReason);

            return evaluationRepository.save(evaluation);
        }

        // BƯỚC 1: CHUẨN HÓA (Dùng dữ liệu all-time sales)
        double rScore = 0.5;
        if (ratingCount > 0) {
            rScore = product.getRatingAvg() != null ? product.getRatingAvg().doubleValue() / 5.0 : 0.5;
        }
        double sScore = ctx.sMax > 0 ? (double) totalSales / ctx.sMax : 0;

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

    // Tối ưu: evaluate Fuzzy dùng map thay vì query từng sản phẩm
    private ProductEvaluation evaluateFuzzyWithMap(Product product, CategoryContext ctx, int totalSales,
                                                   Map<Integer, ProductEvaluation> evalMap) {
        double pScore = 0.5;
        if (ctx.pMax != null && ctx.pMin != null && ctx.pMax.compareTo(ctx.pMin) > 0 && product.getPrice() != null) {
            pScore = product.getPrice().subtract(ctx.pMin)
                    .divide(ctx.pMax.subtract(ctx.pMin), 4, RoundingMode.HALF_UP).doubleValue();
        }

        int ratingCount = product.getRatingCount() != null ? product.getRatingCount() : 0;

        if (ratingCount == 0 && totalSales == 0) {
            long daysSinceCreation = product.getCreatedAt() != null ? java.time.temporal.ChronoUnit.DAYS.between(product.getCreatedAt(), LocalDateTime.now()) : 30;
            boolean isNewArrival = daysSinceCreation <= 14;
            
            String xaiReason = isNewArrival ? 
                "Sản phẩm mới ra mắt: Đang chờ những đánh giá đầu tiên từ cộng đồng người dùng." : 
                "Chưa có đủ dữ liệu đánh giá: Sản phẩm đang chờ được khám phá và trải nghiệm.";

            ProductEvaluation evaluation = evalMap.getOrDefault(product.getId(), new ProductEvaluation());
            evaluation.setProduct(product);
            evaluation.setRatingScore(BigDecimal.valueOf(0.5));
            evaluation.setSoldScore(BigDecimal.ZERO);
            evaluation.setPriceScore(BigDecimal.valueOf(pScore));
            evaluation.setScore(BigDecimal.valueOf(0.5));
            evaluation.setType(ProductEvaluationType.FUZZY);
            evaluation.setReason(xaiReason);

            return evaluationRepository.save(evaluation);
        }

        // BƯỚC 1: CHUẨN HÓA (Dùng dữ liệu all-time)
        double rScore = 0.5;
        if (ratingCount > 0) {
            rScore = product.getRatingAvg() != null ? product.getRatingAvg().doubleValue() / 5.0 : 0.5;
        }
        double sScore = ctx.sMax > 0 ? (double) totalSales / ctx.sMax : 0;

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
        rules.add(new FuzzyRule("L12", "Sản phẩm nhận nhiều phản hồi chưa tốt từ người dùng, bạn nên cân nhắc kỹ trước khi quyết định mua hàng.", 0.30, Math.min(muPoorR, 1.0)));
        rules.add(new FuzzyRule("L13", "Sản phẩm có chất lượng ở mức cơ bản, đáp ứng được các nhu cầu mua sắm và sử dụng phổ thông.", 0.50, Math.min(muAvgR, 1.0)));
        rules.add(new FuzzyRule("L14", "Sản phẩm có chất lượng tốt, nhận được nhiều phản hồi tích cực và sự tin tưởng từ cộng đồng người dùng.", 0.68, Math.min(muGoodR, 1.0)));

        FuzzyRule dominantRule = rules.getFirst();
        for (FuzzyRule rule : rules) {
            if (rule.firingStrength > dominantRule.firingStrength) dominantRule = rule;
        }

        double numerator = 0;
        double denominator = 0;
        for (FuzzyRule rule : rules) {
            numerator += rule.firingStrength * rule.centroidValue;
            denominator += rule.firingStrength;
        }

        double finalScore = denominator > 0 ? numerator / denominator : 0.5;
        String xaiReason = denominator > 0 ? dominantRule.reason : "Hệ thống đang phân tích thêm dữ liệu để đánh giá.";

        // Dùng map thay vì query
        ProductEvaluation evaluation = evalMap.getOrDefault(product.getId(), new ProductEvaluation());
        evaluation.setProduct(product);
        evaluation.setRatingScore(BigDecimal.valueOf(rScore));
        evaluation.setSoldScore(BigDecimal.valueOf(sScore));
        evaluation.setPriceScore(BigDecimal.valueOf(pScore));
        evaluation.setScore(BigDecimal.valueOf(finalScore));
        evaluation.setType(ProductEvaluationType.FUZZY);
        evaluation.setReason(xaiReason);

        return evaluationRepository.save(evaluation);
    }

    // Tối ưu: evaluate Trending dùng map thay vì query từng sản phẩm
    private ProductEvaluation evaluateTrendingWithMap(Product product, int salesInMonth, double pScore,
                                                      Map<Integer, ProductEvaluation> evalMap) {
        double salesFactor = Math.min((double) salesInMonth / 100.0, 1.0);
        
        int ratingCount = product.getRatingCount() != null ? product.getRatingCount() : 0;
        double ratingFactor = 0.5;
        if (ratingCount > 0) {
            ratingFactor = product.getRatingAvg() != null ? product.getRatingAvg().doubleValue() / 5.0 : 0.5;
        }

        long daysSinceCreation = product.getCreatedAt() != null ? java.time.temporal.ChronoUnit.DAYS.between(product.getCreatedAt(), LocalDateTime.now()) : 30;
        boolean isNewArrival = daysSinceCreation <= 14;

        double trendingScore;
        String xaiReason;

        if (salesInMonth == 0) {
            if (isNewArrival) {
                trendingScore = 0.5 + (ratingFactor * 0.2);
                xaiReason = "Sản phẩm mới ra mắt: Đang thu hút sự chú ý, hãy là người đầu tiên trải nghiệm!";
            } else {
                trendingScore = ratingFactor * 0.1;
                xaiReason = "Sản phẩm mới: Đang chờ lượt trải nghiệm từ cộng đồng.";
            }
        } else {
            trendingScore = (salesFactor * 0.8) + (ratingFactor * 0.2);
            if (isNewArrival) {
                trendingScore = Math.min(trendingScore + 0.15, 1.0);
            }
            xaiReason = (salesInMonth >= 50) ? "Xu hướng tháng: Đang cực hot với hơn " + salesInMonth + " lượt bán." :
                    (salesInMonth >= 10) ? "Đang tăng trưởng: Lượt mua tăng ổn định trong tháng." :
                            "Tiềm năng: Đang có lượt bán và phản hồi tốt.";
        }

        // Dùng map thay vì query
        ProductEvaluation evaluation = evalMap.getOrDefault(product.getId(), new ProductEvaluation());
        evaluation.setProduct(product);
        evaluation.setScore(BigDecimal.valueOf(trendingScore));
        evaluation.setType(ProductEvaluationType.TRENDING);
        evaluation.setReason(xaiReason);
        evaluation.setSoldScore(BigDecimal.valueOf(salesFactor));
        evaluation.setRatingScore(BigDecimal.valueOf(ratingFactor));
        evaluation.setPriceScore(BigDecimal.valueOf(pScore));

        return evaluationRepository.save(evaluation);
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