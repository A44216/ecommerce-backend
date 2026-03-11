package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.requests.OrderItemRequest;
import com.ecommerce.backend.dto.responses.OrderItemResponse;
import com.ecommerce.backend.service.OrderItemService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-items")
@CrossOrigin
public class OrderItemController {

    private final OrderItemService orderItemService;

    public OrderItemController(OrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }

    @GetMapping
    public List<OrderItemResponse> getAllOrderItems() {
        return orderItemService.getAllOrderItems();
    }

    @GetMapping("/{id}")
    public OrderItemResponse getOrderItemById(@PathVariable Integer id) {
        return orderItemService.getOrderItemById(id);
    }

    @GetMapping("/order/{orderId}")
    public List<OrderItemResponse> getItemsByOrder(@PathVariable Integer orderId) {
        return orderItemService.getItemsByOrder(orderId);
    }

    @PostMapping
    public OrderItemResponse createOrderItem(@Valid @RequestBody OrderItemRequest request) {
        return orderItemService.createOrderItem(request);
    }

    @DeleteMapping("/{id}")
    public void deleteOrderItem(@PathVariable Integer id) {
        orderItemService.deleteOrderItem(id);
    }
}