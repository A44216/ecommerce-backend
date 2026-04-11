package com.ecommerce.backend.service.seller;

import com.ecommerce.backend.dto.requests.seller.review.SellerReplyRequest;
import com.ecommerce.backend.dto.responses.seller.PageResponse;
import com.ecommerce.backend.dto.responses.seller.review.SellerReviewResponse;
import com.ecommerce.backend.entity.Review;
import com.ecommerce.backend.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerReviewService {

    private final ReviewRepository reviewRepository;
    private final SellerShopService sellerShopService;

    public PageResponse<SellerReviewResponse> getReviews(
            Integer productId,
            Boolean isReplied,
            int page,
            int size,
            String sort
    ) {
        if (productId == null || productId <= 0) {
            throw new RuntimeException("productId invalid");
        }

        Integer shopId = sellerShopService.getMyShop().getId();

        Sort sortObj = buildSort(sort);

        PageRequest pageRequest = PageRequest.of(page, size, sortObj);

        Page<Review> reviews = reviewRepository.findByShopFilter(
                shopId,
                productId,
                isReplied,
                pageRequest
        );

        PageResponse<SellerReviewResponse> res = new PageResponse<>();

        res.setItems(reviews.map(this::mapToResponse).getContent());
        res.setPage(reviews.getNumber());
        res.setSize(reviews.getSize());
        res.setTotalElements(reviews.getTotalElements());
        res.setTotalPages(reviews.getTotalPages());

        return res;
    }

    private Sort buildSort(String sort) {

        if (sort == null || sort.isBlank()) {
            return Sort.by(
                    Sort.Order.desc("rating"),
                    Sort.Order.desc("createdAt")
            );
        }

        List<Sort.Order> orders = new ArrayList<>();

        for (String p : sort.split(",")) {
            String[] kv = p.split("_");

            String field = kv[0];
            String direction = kv.length > 1 ? kv[1] : "desc";

            switch (field) {
                case "time" -> field = "createdAt";
                case "rating" -> field = "rating";
                default -> throw new RuntimeException("Invalid sort field: " + field);
            }

            Sort.Direction dir = direction.equalsIgnoreCase("asc")
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;

            orders.add(new Sort.Order(dir, field));
        }

        return Sort.by(orders);
    }


    public void replyReview(Integer reviewId, SellerReplyRequest request) {

        Integer shopId = sellerShopService.getMyShop().getId();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getProduct().getShop().getId().equals(shopId)) {
            throw new RuntimeException("You cannot reply this review");
        }

        if (review.getSellerReply() != null) {
            throw new RuntimeException("Already replied");
        }

        review.setSellerReply(request.getSellerReply());
        review.setSellerReplyAt(LocalDateTime.now());

        reviewRepository.save(review);
    }

    private SellerReviewResponse mapToResponse(Review r) {

        SellerReviewResponse res = new SellerReviewResponse();

        // REVIEW
        res.setReviewId(r.getId());
        res.setRating(r.getRating());
        res.setComment(r.getComment());
        res.setCreatedAt(r.getCreatedAt());

        // USER
        res.setFullName(r.getUser().getFullName());
        res.setUserAvatar(r.getUser().getAvatar());

        // REPLY
        res.setSellerReply(r.getSellerReply());
        res.setSellerReplyAt(r.getSellerReplyAt());

        // STATUS
        res.setIsReplied(r.getSellerReply() != null);

        return res;
    }
}