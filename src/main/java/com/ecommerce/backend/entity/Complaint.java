package com.ecommerce.backend.entity;

import com.ecommerce.backend.enums.ComplaintStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "complaints",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_complaints_complaint_code", columnNames = "complaint_code")
        },
        indexes = {
                @Index(name = "idx_complaints_user", columnList = "user_id"),
                @Index(name = "idx_complaints_resolved_by", columnList = "resolved_by")
        }
)
@Check(name = "chk_complaint_resolution", constraints = "((status = 'PENDING' and resolved_by is null and resolved_at is null and admin_response is null) or (status in ('RESOLVED','REJECTED') and resolved_by is not null and resolved_at is not null and admin_response is not null and trim(admin_response) <> ''))")
@Getter
@Setter
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'PENDING'")
    private ComplaintStatus status = ComplaintStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false, columnDefinition = "TIMESTAMP")
    @ColumnDefault("CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "resolved_at", columnDefinition = "TIMESTAMP")
    @ColumnDefault("NULL")
    private LocalDateTime resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @Column(name = "admin_response", columnDefinition = "TEXT")
    private String adminResponse;

    @Column(name = "complaint_code", nullable = false, length = 30)
    private String complaintCode;
}