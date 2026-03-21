package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    // đơn hàng của user
    List<Order> findByUserId(Integer userId);

    // đơn hàng theo trạng thái
    List<Order> findByStatus(OrderStatus status);

    // đơn hàng của user theo trạng thái
    List<Order> findByUserIdAndStatus(Integer userId, OrderStatus status);
}