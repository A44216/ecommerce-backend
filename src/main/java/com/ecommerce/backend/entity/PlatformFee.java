package com.ecommerce.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "platform_fees")
public class PlatformFee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal rate = BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false, columnDefinition = "TINYINT(1)")
    @ColumnDefault("1")
    private Boolean isActive = true;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
    @ColumnDefault("CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
}
