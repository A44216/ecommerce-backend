package com.ecommerce.backend.controller.seller;

import com.ecommerce.backend.dto.requests.seller.product.SellerProductRequest;
import com.ecommerce.backend.dto.responses.seller.product.SellerProductResponse;
import com.ecommerce.backend.service.seller.SellerProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seller/products")
public class SellerProductController {

    private final SellerProductService productService;

    // Constructor Injection
    public SellerProductController(SellerProductService productService) {
        this.productService = productService;
    }

    // lấy danh sách tất cả sản phẩm
    // API: GET /api/products
    @GetMapping
    public ResponseEntity<List<SellerProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // lấy thông tin chi tiết một sản phẩm theo ID
    // API: GET /api/products/{id}
    @GetMapping("/{id}")
    public ResponseEntity<SellerProductResponse> getProductById(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/shop/{shopId}")
    public List<SellerProductResponse> getProductByShop(@PathVariable Integer shopId) {
        return productService.getProductsByShop(shopId);
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

    // tìm kiếm sản phẩm theo tên
    // API: GET /api/products/search?keyword=...
//    @GetMapping("/search")
//    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String keyword) {
//        return ResponseEntity.ok(productService.searchProducts(keyword));
//    }

    // lấy danh sách sản phẩm theo Category (Danh mục)
    // API: GET /api/products/category/{categoryId}
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<SellerProductResponse>> getProductsByCategory(@PathVariable Integer categoryId) {
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
    public ResponseEntity<List<SellerProductResponse>> getDeletedProducts() {
        return ResponseEntity.ok(productService.getDeletedProducts());
    }

    @GetMapping("/search")
    public ResponseEntity<List<SellerProductResponse>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer shopId
    ) {
        return ResponseEntity.ok(
                productService.searchProducts(keyword, shopId)
        );
    }

}