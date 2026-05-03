package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.requests.ProductEvaluationRequest;
import com.ecommerce.backend.dto.responses.ProductEvaluationResponse;
import com.ecommerce.backend.service.ProductEvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-evaluations")
@RequiredArgsConstructor

public class ProductEvaluationController {

    private final ProductEvaluationService service;

    // Lấy danh sách Top Deal hiển thị lên trang chủ
    @GetMapping("/top-deals")
    public List<ProductEvaluationResponse> getTopDeals(
            @RequestParam(defaultValue = "20") int limit) {
        return service.getTopFuzzyDeals(limit);
    }

    // Chấm điểm Fuzzy cho 1 sản phẩm cụ thể
    @PostMapping("/evaluate")
    public ProductEvaluationResponse evaluateSingleProduct(
            @RequestBody @Valid ProductEvaluationRequest request) {
        return service.evaluateProduct(request);
    }

    // Ép hệ thống chạy lại toàn bộ thuật toán Fuzzy ngay lập tức
    @PostMapping("/generate-all")
    public String generateAll() {
        service.generateGlobalFuzzyEvaluations();
        return "Đã kích hoạt chạy thuật toán đánh giá Fuzzy cho toàn bộ sản phẩm!";
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.deleteEvaluation(id);
    }

    @GetMapping("/trending")
    public List<ProductEvaluationResponse> getTrendingDeals(
            @RequestParam(defaultValue = "20") int limit) {
        return service.getTopTrendingDeals(limit);
    }

}