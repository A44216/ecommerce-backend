package com.ecommerce.backend.controller.admin;

import com.ecommerce.backend.dto.responses.admin.shop.AdminShopDetailResponse;
import com.ecommerce.backend.dto.responses.admin.shop.AdminShopResponse;
import com.ecommerce.backend.dto.responses.seller.PageResponse;
import com.ecommerce.backend.enums.ShopStatus;
import com.ecommerce.backend.service.admin.AdminShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin/shops")
@RequiredArgsConstructor
public class AdminShopController {

    private final AdminShopService adminShopService;

    @GetMapping
    public ResponseEntity<PageResponse<AdminShopResponse>> getShops(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ShopStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(adminShopService.getShops(page, size, status, keyword, sortBy, sortDir));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminShopDetailResponse> getShopById(@PathVariable Integer id) {
        return ResponseEntity.ok(adminShopService.getShopById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateShopStatus(
            @PathVariable Integer id,
            @RequestParam ShopStatus status) {
        adminShopService.updateShopStatus(id, status);
        return ResponseEntity.noContent().build();
    }
}
