package com.ecommerce.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

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

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

}
