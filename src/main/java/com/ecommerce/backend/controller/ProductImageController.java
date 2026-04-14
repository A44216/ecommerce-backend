package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.requests.ProductImageRequest;
import com.ecommerce.backend.dto.responses.ProductImageResponse;
import com.ecommerce.backend.service.ProductImageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-images")
public class ProductImageController {

    private final ProductImageService productImageService;

    public ProductImageController(ProductImageService productImageService) {
        this.productImageService = productImageService;
    }

    // Lấy tất cả ảnh của một sản phẩm
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductImageResponse>> getImagesByProductId(@PathVariable Integer productId) {
        return ResponseEntity.ok(productImageService.getImagesByProductId(productId));
    }

    // Thêm ảnh mới
    @PostMapping
    public ResponseEntity<ProductImageResponse> addProductImage(@Valid @RequestBody ProductImageRequest request) {
        return new ResponseEntity<>(productImageService.addProductImage(request), HttpStatus.CREATED);
    }

    // Xóa ảnh
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductImage(@PathVariable Integer id) {
        productImageService.deleteProductImage(id);
        return ResponseEntity.noContent().build();
    }
}