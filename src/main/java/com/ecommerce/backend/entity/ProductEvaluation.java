package com.ecommerce.backend.entity;

import com.ecommerce.backend.enums.ProductEvaluationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "product_evaluations",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_product_type", columnNames = {"product_id", "type"})
        },
        indexes = {
                @Index(name = "idx_evaluations_type", columnList = "type")
        }
)
@Getter
@Setter
public class ProductEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductEvaluationType type;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal score;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String reason;

    @Column(name = "sold_score", precision = 5, scale = 4, nullable = false)
    private BigDecimal soldScore;

    @Column(name = "rating_score", precision = 5, scale = 4, nullable = false)
    private BigDecimal ratingScore;

    @Column(name = "price_score", precision = 5, scale = 4, nullable = false)
    private BigDecimal priceScore;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}