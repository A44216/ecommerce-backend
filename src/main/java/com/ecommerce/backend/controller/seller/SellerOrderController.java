package com.ecommerce.backend.controller.seller;

import com.ecommerce.backend.dto.responses.seller.PageResponse;
import com.ecommerce.backend.dto.responses.seller.order.SellerOrderDetailResponse;
import com.ecommerce.backend.dto.responses.seller.order.SellerOrderResponse;
import com.ecommerce.backend.enums.OrderStatus;
import com.ecommerce.backend.enums.PaymentMethod;
import com.ecommerce.backend.enums.PaymentStatus;
import com.ecommerce.backend.service.OrderService;
import com.ecommerce.backend.service.seller.SellerOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("hasRole('SELLER')")
@RestController
@RequestMapping("api/seller/orders")
@RequiredArgsConstructor
public class SellerOrderController {

    private final SellerOrderService service;
    private final OrderService orderService;

    @GetMapping
    public PageResponse<SellerOrderResponse> getOrders(
            @RequestParam(name = "status", required = false) List<OrderStatus> statuses,
            @RequestParam(required = false) PaymentMethod paymentMethod,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.getOrders(statuses, paymentMethod, paymentStatus, keyword, page, size);
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

    @GetMapping("/autocomplete")
    public List<String> autocomplete(@RequestParam(required = false) String keyword) {
        return service.autocompleteOrders(keyword);
    }

    @PutMapping("/{id}/accept-return")
    public void acceptReturn(@PathVariable Integer id) {
        orderService.acceptReturn(id);
    }

    @PutMapping("/{id}/reject-return")
    public void rejectReturn(@PathVariable Integer id, @RequestParam String reason) {
        orderService.rejectReturn(id, reason);
    }

}