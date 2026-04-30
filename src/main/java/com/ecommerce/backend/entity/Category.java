package com.ecommerce.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "categories",
        indexes = {
                @Index(name = "idx_categories_not_deleted_name", columnList = "is_deleted, name")
        },

        uniqueConstraints = {
                @UniqueConstraint(name = "unique_categories_name", columnNames = "name")
        }
)
@Getter
@Setter
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

}