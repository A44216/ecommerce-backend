package com.ecommerce.backend.entity;

import com.ecommerce.backend.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        name = "products",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_products_product_code", columnNames = "product_code")
        },
        indexes = {

                // shop_id, is_deleted, status
                @Index(name = "idx_products_shop_filter", columnList = "shop_id, is_deleted, status"),

                // shop_id, category
                @Index( name = "idx_products_shop_category", columnList = "shop_id, category_id, is_deleted"),

                // category_id, name
                @Index(name = "idx_products_category_name", columnList = "category_id, name"),

                // sold_count
                @Index(name = "idx_products_sold", columnList = "sold_count"),

                // created_at
                @Index(name = "idx_products_created", columnList = "created_at"),

                @Index(name = "idx_products_category_filter", columnList = "category_id, is_deleted, created_at"),
                @Index(name = "idx_products_shop_created", columnList = "shop_id, created_at"),
                @Index(name = "idx_products_category_sold", columnList = "category_id, sold_count"),

                @Index(name = "idx_products_name", columnList = "name"),
                @Index(name = "idx_products_shop", columnList = "shop_id"),
                @Index(name = "idx_products_price", columnList = "price"),
                @Index(name = "idx_products_category", columnList = "category_id"),
                @Index(name = "idx_products_search", columnList = "name, price"),
                @Index(name = "idx_products_status", columnList = "status")
        }
)
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.PENDING;

    @Column(name = "rating_avg", precision = 3, scale = 2, nullable = false)
    private BigDecimal ratingAvg = BigDecimal.ZERO;

    @Column(name = "rating_count")
    private Integer ratingCount = 0;

    @Column(name = "sold_count")
    private Integer soldCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @BatchSize(size = 20)
    private List<ProductImage> images;

    @Column(name = "product_code", nullable = false, unique = true, length = 30)
    private String productCode;

}