package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.OrderItemRequest;
import com.ecommerce.backend.dto.responses.OrderItemResponse;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderItem;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.repository.OrderItemRepository;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderItemService(OrderItemRepository orderItemRepository,
                            OrderRepository orderRepository,
                            ProductRepository productRepository) {
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    private OrderItemResponse mapToDTO(OrderItem item) {

        return new OrderItemResponse(
                item.getId(),
                item.getProduct().getName(),
                item.getPrice(),
                item.getQuantity()
        );
    }

    public List<OrderItemResponse> getAllOrderItems() {
        return orderItemRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public OrderItemResponse getOrderItemById(Integer id) {

        OrderItem item = orderItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OrderItem not found"));

        return mapToDTO(item);
    }

    public List<OrderItemResponse> getItemsByOrder(Integer orderId) {

        return orderItemRepository.findByOrderId(orderId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public OrderItemResponse createOrderItem(OrderItemRequest request) {

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        OrderItem item = new OrderItem();

        item.setOrder(order);
        item.setProduct(product);
        item.setPrice(request.getPrice());
        item.setQuantity(request.getQuantity());

        return mapToDTO(orderItemRepository.save(item));
    }

    public void deleteOrderItem(Integer id) {

        if (!orderItemRepository.existsById(id)) {
            throw new RuntimeException("OrderItem not found");
        }

        orderItemRepository.deleteById(id);
    }
}