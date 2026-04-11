package com.ecommerce.backend.controller.seller;

import com.ecommerce.backend.dto.responses.seller.PageResponse;
import com.ecommerce.backend.dto.responses.seller.order.SellerOrderDetailResponse;
import com.ecommerce.backend.dto.responses.seller.order.SellerOrderResponse;
import com.ecommerce.backend.enums.OrderStatus;
import com.ecommerce.backend.service.seller.SellerOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("hasRole('SELLER')")
@RestController
@RequestMapping("api/seller/orders")
@RequiredArgsConstructor
public class SellerOrderController {

    private final SellerOrderService service;

    @GetMapping
    public PageResponse<SellerOrderResponse> getOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.getOrders(status, page, size);
    }

    @GetMapping("/{id}")
    public SellerOrderDetailResponse getDetail(@PathVariable Integer id) {
        return service.getOrderDetail(id);
    }

    @PutMapping("/{id}/status")
    public void updateStatus(
            @PathVariable Integer id,
            @RequestParam OrderStatus status
    ) {
        service.updateOrderStatus(id, status);
    }
}