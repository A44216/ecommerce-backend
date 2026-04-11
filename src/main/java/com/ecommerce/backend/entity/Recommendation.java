package com.ecommerce.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "recommendations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_recommendations_user_product",
                        columnNames = {"user_id","product_id"}
                )
        },
        indexes = {
                @Index(name = "idx_recommendations_product", columnList = "product_id"),
                @Index(name = "idx_recommendations_user", columnList = "user_id")
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

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal score;
}