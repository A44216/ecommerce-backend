package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.responses.seller.order.SellerOrderItemResponse;
import com.ecommerce.backend.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-items")
@RequiredArgsConstructor
public class OrderItemController {

    private final OrderItemService service;

    @GetMapping("/order/{orderId}")
    public List<SellerOrderItemResponse> getItemsByOrder(
            @PathVariable Integer orderId,
            @RequestParam Integer shopId
    ) {
        return service.getItemsByOrder(orderId, shopId);
    }
}