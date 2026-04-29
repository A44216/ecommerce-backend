package com.ecommerce.backend.entity;

import com.ecommerce.backend.enums.AdminActionType;
import com.ecommerce.backend.enums.EntityType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "admin_actions",
        indexes = {
                @Index(name = "idx_entity", columnList = "entity_type, entity_id")
        }
)
@Getter
@Setter
public class AdminAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private EntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private Integer entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminActionType action;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}