package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.requests.ProductRequest;
import com.ecommerce.backend.dto.responses.ProductResponse;
import com.ecommerce.backend.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    // Constructor Injection
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // lấy danh sách tất cả sản phẩm
    // API: GET /api/products
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // API: GET /api/products/page?page=0&size=10
    @GetMapping("/page")
    public ResponseEntity<com.ecommerce.backend.dto.responses.PageResponse<ProductResponse>> getProductsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy) {
        return ResponseEntity.ok(productService.getAllProductsPaginated(page, size, sortBy));
    }

    @GetMapping("/search/page")
    public ResponseEntity<com.ecommerce.backend.dto.responses.PageResponse<ProductResponse>> searchProductsPaginated(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy) {
        return ResponseEntity.ok(productService.searchProductsPaginated(keyword, page, size, sortBy));
    }

    @GetMapping("/category/{categoryId}/page")
    public ResponseEntity<com.ecommerce.backend.dto.responses.PageResponse<ProductResponse>> getProductsByCategoryPaginated(
            @PathVariable Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy) {
        return ResponseEntity.ok(productService.getProductsByCategoryPaginated(categoryId, page, size, sortBy));
    }

    // lấy thông tin chi tiết một sản phẩm theo ID
    // API: GET /api/products/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/shop/{shopId}")
    public List<ProductResponse> getProductByShop(@PathVariable Integer shopId) {
        return productService.getProductsByShop(shopId);
    }

    // tạo sản phẩm mới
    // API: POST /api/products
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse newProduct = productService.createProduct(request);
        // Trả về mã 201 CREATED khi tạo thành công
        return new ResponseEntity<>(newProduct, HttpStatus.CREATED);
    }

    // cập nhật thông tin sản phẩm
    // API: PUT /api/products/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Integer id,
            @Valid @RequestBody ProductRequest request) {
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

    // tìm kiếm sản phẩm theo tên
    // API: GET /api/products/search?keyword=...
//    @GetMapping("/search")
//    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String keyword) {
//        return ResponseEntity.ok(productService.searchProducts(keyword));
//    }

    // lấy danh sách sản phẩm theo Category (Danh mục)
    // API: GET /api/products/category/{categoryId}
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@PathVariable Integer categoryId) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
    }

    // lấy danh sách sản phẩm của một Shop cụ thể
    // API: GET /api/products/shop/{shopId}
//    @GetMapping("/shop/{shopId}")
//    public ResponseEntity<List<ProductResponse>> getProductsByShop(@PathVariable Integer shopId) {
//        return ResponseEntity.ok(productService.getProductsByShop(shopId));
//    }

    @PutMapping("/restore/{id}")
    public ResponseEntity<Void> restoreProduct(@PathVariable Integer id) {
        productService.restoreProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/deleted")
    public ResponseEntity<List<ProductResponse>> getDeletedProducts() {
        return ResponseEntity.ok(productService.getDeletedProducts());
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer shopId
    ) {
        return ResponseEntity.ok(
                productService.searchProducts(keyword, shopId)
        );
    }

    @GetMapping("/suggest")
    public ResponseEntity<List<String>> suggestProducts(@RequestParam String keyword) {
        return ResponseEntity.ok(productService.suggestProductNames(keyword));
    }

    @GetMapping("/trending")
    public ResponseEntity<List<ProductResponse>> getTrendingProducts() {
        return ResponseEntity.ok(productService.getTrendingProducts());
    }

}