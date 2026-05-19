package com.ecommerce.backend.entity;

import com.ecommerce.backend.enums.ProductEvaluationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;

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
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductEvaluationType type;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal score;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String reason;

    @Column(name = "sold_score", precision = 5, scale = 4, nullable = false)
    @ColumnDefault("0.0000")
    private BigDecimal soldScore = BigDecimal.ZERO;

    @Column(name = "rating_score", precision = 5, scale = 4, nullable = false)
    @ColumnDefault("0.0000")
    private BigDecimal ratingScore = BigDecimal.ZERO;

    @Column(name = "price_score", precision = 5, scale = 4, nullable = false)
    @ColumnDefault("0.0000")
    private BigDecimal priceScore = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false, columnDefinition = "TIMESTAMP")
    @ColumnDefault("CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
    @ColumnDefault("CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

}