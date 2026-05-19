package com.ecommerce.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;

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
@Check(name = "chk_reviews_comment_not_blank", constraints = "(comment is null or trim(comment) <> '')")
@Check(name = "chk_reviews_rating", constraints = "rating between 1 and 5")
@Check(name = "chk_reviews_reply_consistency", constraints = "((seller_reply is null and seller_reply_at is null) or (seller_reply is not null and seller_reply_at is not null))")
@Check(name = "chk_reviews_seller_reply_not_blank", constraints = "(seller_reply is null or trim(seller_reply) <> '')")
@Getter
@Setter
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private OrderItem orderItem;

    @Column(nullable = false)
    @ColumnDefault("5")
    private Integer rating = 5;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false, columnDefinition = "TIMESTAMP")
    @ColumnDefault("CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "seller_reply", columnDefinition = "TEXT")
    private String sellerReply;

    @Column(name = "seller_reply_at", columnDefinition = "TIMESTAMP")
    @ColumnDefault("NULL")
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