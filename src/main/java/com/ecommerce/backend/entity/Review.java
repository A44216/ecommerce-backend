package com.ecommerce.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "reviews",
        indexes = {
                @Index(name = "idx_reviews_product_rating", columnList = "product_id, rating"),
                @Index(name = "idx_reviews_user", columnList = "user_id"),
                @Index(name = "idx_reviews_rating", columnList = "rating"),
                @Index(name = "idx_reviews_product_created", columnList = "product_id, created_at"),
                @Index(name = "idx_reviews_product", columnList = "product_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_reviews_order_item", columnNames = "order_item_id")
        }
)
@Getter
@Setter
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)    private OrderItem orderItem;

    @Column(nullable = false)
    private Integer rating = 5;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "seller_reply", columnDefinition = "TEXT")
    private String sellerReply;

    @Column(name = "seller_reply_at")
    private LocalDateTime sellerReplyAt;

    @PrePersist
    @PreUpdate
    private void validateReply() {
        if (sellerReply != null && sellerReplyAt == null) {
            sellerReplyAt = LocalDateTime.now();
        }

        if (sellerReply == null && sellerReplyAt != null) {
            throw new RuntimeException("sellerReply null thì sellerReplyAt phải null");
        }
    }

}