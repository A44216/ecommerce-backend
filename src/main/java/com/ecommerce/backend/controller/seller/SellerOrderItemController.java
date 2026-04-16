package com.ecommerce.backend.controller.seller;

import com.ecommerce.backend.dto.responses.seller.order.SellerOrderItemResponse;
import com.ecommerce.backend.service.seller.SellerOrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("hasRole('SELLER')")
@RestController
@RequestMapping("/api/seller/order-items")
@RequiredArgsConstructor
public class SellerOrderItemController {

    private final SellerOrderItemService service;

    @GetMapping("/order/{orderId}")
    public List<SellerOrderItemResponse> getItemsByOrder(
            @PathVariable Integer orderId
    ) {
        return service.getItemsByOrder(orderId);
    }
}