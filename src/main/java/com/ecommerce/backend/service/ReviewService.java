package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.ReviewRequest;
import com.ecommerce.backend.dto.responses.ReviewResponse;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.Review;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.entity.OrderItem;
import com.ecommerce.backend.entity.Shop;
import com.ecommerce.backend.repository.OrderItemRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.ReviewRepository;
import com.ecommerce.backend.repository.ShopRepository;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShopRepository shopRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         UserRepository userRepository,
                         ProductRepository productRepository,
                         OrderItemRepository orderItemRepository,
                         ShopRepository shopRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
        this.shopRepository = shopRepository;
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
    private void mapRequestToReview(Review review, ReviewRequest request, User user, Product product, OrderItem orderItem) {
        review.setUser(user);
        review.setProduct(product);
        review.setOrderItem(orderItem);
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

        OrderItem orderItem = orderItemRepository.findById(request.getOrderItemId())
                .orElseThrow(() -> new RuntimeException("Order item not found"));

        Review review = new Review();

        mapRequestToReview(review, request, user, product, orderItem);

        Review saved = reviewRepository.save(review);

        // Update Product stats
        int newProductRatingCount = product.getRatingCount() + 1;
        BigDecimal oldTotalProductRating = product.getRatingAvg().multiply(new BigDecimal(product.getRatingCount()));
        BigDecimal newAvgProductRating = oldTotalProductRating.add(new BigDecimal(request.getRating()))
                .divide(new BigDecimal(newProductRatingCount), 2, java.math.RoundingMode.HALF_UP);
        
        product.setRatingCount(newProductRatingCount);
        product.setRatingAvg(newAvgProductRating);
        productRepository.saveAndFlush(product);

        // Update Shop stats
        Shop shop = product.getShop();
        BigDecimal shopAvg = productRepository.getAverageRatingByShopId(shop.getId());
        Integer shopRatingCount = productRepository.getTotalRatingCountByShopId(shop.getId());
        
        shop.setRatingAvg(shopAvg != null ? shopAvg : BigDecimal.ZERO);
        shop.setRatingCount(shopRatingCount != null ? shopRatingCount : 0);
        shopRepository.save(shop);

        return mapToDTO(saved);
    }

    // xóa review
    @Transactional
    public void deleteReview(Integer id) {
        reviewRepository.deleteById(id);
    }
}