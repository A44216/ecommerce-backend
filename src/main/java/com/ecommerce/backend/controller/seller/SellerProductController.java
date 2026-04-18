package com.ecommerce.backend.controller.seller;

import com.ecommerce.backend.dto.requests.seller.product.SellerProductRequest;
import com.ecommerce.backend.dto.responses.seller.PageResponse;
import com.ecommerce.backend.dto.responses.seller.product.SellerProductResponse;
import com.ecommerce.backend.enums.ProductStatus;
import com.ecommerce.backend.service.seller.SellerProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("hasRole('SELLER')")
@RestController
@RequestMapping("/api/seller/products")
public class SellerProductController {

    private final SellerProductService productService;

    // Constructor Injection
    public SellerProductController(SellerProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<SellerProductResponse>> getProducts(
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                productService.filterProducts(status, keyword, page, size)
        );
    }

    // lấy thông tin chi tiết một sản phẩm theo ID
    // API: GET /api/products/{id}
    @GetMapping("/{id}")
    public ResponseEntity<SellerProductResponse> getProductById(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // tạo sản phẩm mới
    // API: POST /api/products
    @PostMapping
    public ResponseEntity<SellerProductResponse> createProduct(@Valid @RequestBody SellerProductRequest request) {
        SellerProductResponse newProduct = productService.createProduct(request);
        // Trả về mã 201 CREATED khi tạo thành công
        return new ResponseEntity<>(newProduct, HttpStatus.CREATED);
    }

    // cập nhật thông tin sản phẩm
    // API: PUT /api/products/{id}
    @PutMapping("/{id}")
    public ResponseEntity<SellerProductResponse> updateProduct(
            @PathVariable Integer id,
            @Valid @RequestBody SellerProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    // xóa sản phẩm
    // API: DELETE /api/products/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        productService.deleteProduct(id);
        // Trả về mã 204 NO CONTENT khi xóa thành công (không có body trả về)
        return ResponseEntity.noContent().build();
    }

    // lấy danh sách sản phẩm theo Category (Danh mục)
    // API: GET /api/products/category/{categoryId}
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<PageResponse<SellerProductResponse>> getProductsByCategory(
            @PathVariable Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                productService.getProductsByCategory(categoryId, page, size)
        );
    }

    @PutMapping("/restore/{id}")
    public ResponseEntity<Void> restoreProduct(@PathVariable Integer id) {
        productService.restoreProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/deleted")
    public ResponseEntity<PageResponse<SellerProductResponse>> getDeletedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                productService.getDeletedProducts(page, size)
        );
    }

    @PutMapping("/submit/{id}")
    public ResponseEntity<Void> submitProduct(@PathVariable Integer id) {
        productService.submitProduct(id);
        return ResponseEntity.noContent().build();
    }

}