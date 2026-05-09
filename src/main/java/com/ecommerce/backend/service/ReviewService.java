package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.ReviewRequest;
import com.ecommerce.backend.dto.responses.ReviewResponse;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.Review;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.ReviewRepository;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         UserRepository userRepository,
                         ProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    // ENTITY -> RESPONSE DTO
    private ReviewResponse mapToDTO(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .userName(review.getUser().getFullName())
                .productId(review.getProduct().getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }

    // REQUEST DTO -> ENTITY
    private void mapRequestToReview(Review review, ReviewRequest request, User user, Product product) {
        review.setUser(user);
        review.setProduct(product);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
    }

    // tất cả review
    public List<ReviewResponse> getAllReviews() {

        List<Review> reviews = reviewRepository.findAll();

        return reviews.stream()
                .map(this::mapToDTO)
                .toList();
    }

    // review theo product
    public List<ReviewResponse> getReviewsByProduct(Integer productId) {

        List<Review> reviews = reviewRepository.findByProductId(productId);

        return reviews.stream()
                .map(this::mapToDTO)
                .toList();
    }

    // tạo review
    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Review review = new Review();

        mapRequestToReview(review, request, user, product);

        Review saved = reviewRepository.save(review);

        return mapToDTO(saved);
    }

    // xóa review
    @Transactional
    public void deleteReview(Integer id) {
        reviewRepository.deleteById(id);
    }
}