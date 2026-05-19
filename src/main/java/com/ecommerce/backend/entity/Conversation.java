package com.ecommerce.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "conversations",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_conversations_customer_shop", columnNames = {"customer_id","shop_id"})
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
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Shop shop;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false, columnDefinition = "TIMESTAMP")
    @ColumnDefault("CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
}