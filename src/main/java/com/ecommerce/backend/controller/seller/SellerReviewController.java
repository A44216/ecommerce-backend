package com.ecommerce.backend.controller.seller;

import com.ecommerce.backend.dto.requests.seller.review.SellerReplyRequest;
import com.ecommerce.backend.dto.responses.seller.PageResponse;
import com.ecommerce.backend.dto.responses.seller.review.SellerReviewResponse;
import com.ecommerce.backend.service.seller.SellerReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("hasRole('SELLER')")
@RestController
@RequestMapping("/api/seller/reviews")
@RequiredArgsConstructor
public class SellerReviewController {

    private final SellerReviewService sellerReviewService;

    @GetMapping
    public PageResponse<SellerReviewResponse> getReviews(
            @RequestParam Integer productId,
            @RequestParam(required = false) Boolean isReplied,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "rating_desc,time_desc") String sort
    ) {
        return sellerReviewService.getReviews(productId, isReplied, page, size, sort);
    }

    @PostMapping("/{reviewId}/reply")
    public void replyReview(
            @PathVariable Integer reviewId,
            @RequestBody SellerReplyRequest request
    ) {
        sellerReviewService.replyReview(reviewId, request);
    }
}