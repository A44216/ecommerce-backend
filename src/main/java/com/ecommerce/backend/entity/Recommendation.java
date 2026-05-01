package com.ecommerce.backend.entity;

import com.ecommerce.backend.enums.RecommendationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "recommendations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_user_product_type",
                        columnNames = {"user_id", "product_id", "type"}
                )
        },
        indexes = {
                @Index(name = "idx_recommendations_product", columnList = "product_id"),
                @Index(name = "idx_recommendations_user", columnList = "user_id"),
                @Index(name = "idx_recommendations_type", columnList = "type")
        }
)
@Getter
@Setter
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecommendationType type;

    @Lob
    private String reason;

    @Column(name = "sold_score", precision = 5, scale = 4, nullable = false)
    private BigDecimal soldScore;

    @Column(name = "rating_score", precision = 5, scale = 4, nullable = false)
    private BigDecimal ratingScore;

    @Column(name = "price_score", precision = 5, scale = 4, nullable = false)
    private BigDecimal priceScore;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}