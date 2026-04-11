package com.ecommerce.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "conversations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_conversations_customer_shop",
                        columnNames = {"customer_id","shop_id"}
                )
        },
        indexes = {
                @Index(name = "idx_conversations_customer", columnList = "customer_id"),
                @Index(name = "idx_conversations_shop", columnList = "shop_id")
        }
)
@Getter
@Setter
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}