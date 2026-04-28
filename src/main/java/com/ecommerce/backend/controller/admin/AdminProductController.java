package com.ecommerce.backend.controller.admin;

import com.ecommerce.backend.dto.responses.admin.product.AdminProductDetailResponse;
import com.ecommerce.backend.dto.responses.admin.product.AdminProductResponse;
import com.ecommerce.backend.dto.responses.seller.PageResponse;
import com.ecommerce.backend.enums.ProductStatus;
import com.ecommerce.backend.service.admin.AdminProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;

    @GetMapping
    public ResponseEntity<PageResponse<AdminProductResponse>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer shopId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(adminProductService.getProducts(page, size, shopId, categoryId, status, keyword));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminProductDetailResponse> getProductById(@PathVariable Integer id) {
        return ResponseEntity.ok(adminProductService.getProductById(id));
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<List<String>> autocomplete(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer shopId
    ) {
        return ResponseEntity.ok(
                adminProductService.autocomplete(keyword, shopId)
        );
    }

}
