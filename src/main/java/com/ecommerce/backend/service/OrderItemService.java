package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.responses.seller.order.SellerOrderItemResponse;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderItem;
import com.ecommerce.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemService {

    private final OrderRepository orderRepository;

    public List<SellerOrderItemResponse> getItemsByOrder(Integer orderId, Integer shopId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getShop().getId().equals(shopId)) {
            throw new RuntimeException("Not allowed");
        }

        return order.getItems().stream()
                .map(this::mapToDTO)
                .toList();
    }

    private SellerOrderItemResponse mapToDTO(OrderItem item) {
        return SellerOrderItemResponse.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProductName())
                .productImage(item.getProductImage())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .build();
    }
}
