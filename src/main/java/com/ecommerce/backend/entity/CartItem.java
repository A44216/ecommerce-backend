package com.ecommerce.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
        name = "cart_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_cart_items_cart_product", columnNames = {"cart_id","product_id"})
        },
        indexes = {
                @Index(name = "idx_cart_items_product", columnList = "product_id")
        }
)
@Check(name = "chk_cart_items_quantity", constraints = "quantity >= 1")
@Getter
@Setter
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Product product;

    @Column(nullable = false)
    @ColumnDefault("1")
    private Integer quantity = 1;
}