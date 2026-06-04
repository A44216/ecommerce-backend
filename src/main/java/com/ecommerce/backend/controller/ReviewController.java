package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.requests.ReviewRequest;
import com.ecommerce.backend.dto.responses.ReviewResponse;
import com.ecommerce.backend.service.ReviewService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // tất cả review
    @GetMapping
    public List<ReviewResponse> getAllReviews() {
        return reviewService.getAllReviews();
    }

    // lấy review theo ID
    @GetMapping("/{id}")
    public ReviewResponse getReviewById(@PathVariable Integer id) {
        return reviewService.getReviewById(id);
    }

    // review theo product
    @GetMapping("/product/{productId}")
    public List<ReviewResponse> getReviewsByProduct(@PathVariable Integer productId) {
        return reviewService.getReviewsByProduct(productId);
    }

    // tạo review
    @PostMapping
    public ReviewResponse createReview(@RequestBody ReviewRequest request) {
        return reviewService.createReview(request);
    }

    // xóa review
    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Integer id) {
        reviewService.deleteReview(id);
    }
}