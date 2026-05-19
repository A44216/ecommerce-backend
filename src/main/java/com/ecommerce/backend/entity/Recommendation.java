package com.ecommerce.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "recommendations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "product_id"}, name = "unique_user_product")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recommendation {

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
    Product product;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal score;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    @ColumnDefault("CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
    @ColumnDefault("CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
}