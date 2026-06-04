package com.ecommerce.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(
        name = "order_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_order_items_order_product", columnNames = {"order_id", "product_id"})
        },
        indexes = {
                @Index(name = "idx_order_items_order", columnList = "order_id"),
                @Index(name = "idx_order_items_product", columnList = "product_id")
        }
)
@Check(name = "chk_order_items_price", constraints = "price >= 0")
@Check(name = "chk_order_items_quantity", constraints = "quantity >= 1")
@Getter
@Setter
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    private Product product;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(nullable = false)
    @ColumnDefault("1")
    private Integer quantity = 1;

    @Column(name = "product_name", nullable = false, length = 150)
    private String productName;

    @Column(name = "product_image", length = 255)
    private String productImage;

    @OneToMany(mappedBy = "orderItem", fetch = FetchType.LAZY)
    private List<Review> reviews;
}