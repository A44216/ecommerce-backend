package com.ecommerce.backend.controller.admin;

import com.ecommerce.backend.dto.responses.admin.order.AdminOrderDetailResponse;
import com.ecommerce.backend.dto.responses.admin.order.AdminOrderResponse;
import com.ecommerce.backend.dto.responses.seller.PageResponse;
import com.ecommerce.backend.enums.OrderStatus;
import com.ecommerce.backend.enums.PaymentMethod;
import com.ecommerce.backend.enums.PaymentStatus;
import com.ecommerce.backend.service.OrderService;
import com.ecommerce.backend.service.admin.AdminOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;
    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<PageResponse<AdminOrderResponse>> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer shopId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) PaymentMethod paymentMethod,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        return ResponseEntity.ok(
                adminOrderService.getOrders(
                        page, size,
                        keyword,
                        shopId,
                        status,
                        paymentMethod,
                        paymentStatus,
                        sortBy,
                        direction
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminOrderDetailResponse> getOrderById(@PathVariable Integer id) {
        return ResponseEntity.ok(adminOrderService.getOrderById(id));
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<List<String>> autocomplete(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer shopId
    ) {
        return ResponseEntity.ok(
                adminOrderService.autocomplete(keyword, shopId)
        );
    }

    @PutMapping("/{id}/resolve-dispute")
    public ResponseEntity<?> resolveDispute(@PathVariable Integer id, @RequestParam String decision) {
        return ResponseEntity.ok(orderService.resolveDispute(id, decision));
    }

}