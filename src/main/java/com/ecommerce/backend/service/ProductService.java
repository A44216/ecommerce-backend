package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.ProductRequest;
import com.ecommerce.backend.dto.responses.ProductResponse;
import com.ecommerce.backend.entity.Category;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.ProductImage;
import com.ecommerce.backend.entity.Shop;
import com.ecommerce.backend.enums.ProductStatus;
import com.ecommerce.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ENTITY -> RESPONSE DTO
    private ProductResponse mapToDTO(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .description(product.getDescription())
                .categoryName(product.getCategory().getName())
                .shopName(product.getShop().getShopName())
                .ratingAvg(product.getRatingAvg())
                .ratingCount(product.getRatingCount())
                .soldCount(product.getSoldCount())
                .status(product.getStatus())
                .images(
                        product.getImages() == null ? List.of() :
                                product.getImages().stream()
                                        .map(ProductImage::getImageUrl)
                                        .toList()
                )
                .build();
    }

    // REQUEST DTO -> ENTITY
    private void mapRequestToProduct(Product product, ProductRequest request) {

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setDescription(request.getDescription());

        Category category = new Category();
        category.setId(request.getCategoryId());
        product.setCategory(category);

        Shop shop = new Shop();
        shop.setId(request.getShopId());
        product.setShop(shop);

        product.setStatus(calculateStatus(request.getStock()));

    }

    private ProductStatus calculateStatus(int stock) {
        return stock > 0 ?
                ProductStatus.AVAILABLE :
                ProductStatus.UNAVAILABLE;
    }

    // lấy tất cả sản phẩm
    public List<ProductResponse> getAllProducts() {

        List<Product> products = productRepository.findByIsDeletedFalse();

        return products.stream()
                .map(this::mapToDTO)
                .toList();
    }

    // lấy sản phẩm theo id
    public ProductResponse getProductById(Integer id) {

        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return mapToDTO(product);
    }

    // thêm sản phẩm
    public ProductResponse createProduct(ProductRequest request) {

        Product product = new Product();

        mapRequestToProduct(product, request);

        Product saved = productRepository.save(product);

        return mapToDTO(saved);
    }

    // cập nhật sản phẩm
    public ProductResponse updateProduct(Integer id, ProductRequest request) {

        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        mapRequestToProduct(product, request);

        Product updated = productRepository.save(product);

        return mapToDTO(updated);
    }

    // xóa sản phẩm
    public void deleteProduct(Integer id) {
        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setDeleted(true);

        productRepository.save(product);
    }

    // tìm sản phẩm theo tên
    public List<ProductResponse> searchProducts(String keyword) {

        List<Product> products = productRepository.findByNameContainingIgnoreCaseAndIsDeletedFalse(keyword);

        return products.stream()
                .map(this::mapToDTO)
                .toList();
    }

    // sản phẩm theo category
    public List<ProductResponse> getProductsByCategory(Integer categoryId) {

        List<Product> products = productRepository.findByCategoryIdAndIsDeletedFalse(categoryId);

        return products.stream()
                .map(this::mapToDTO)
                .toList();
    }

    // sản phẩm theo shop
    public List<ProductResponse> getProductsByShop(Integer shopId) {

        List<Product> products = productRepository.findByShopIdAndIsDeletedFalse(shopId);

        return products.stream()
                .map(this::mapToDTO)
                .toList();
    }

    public void restoreProduct(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.isDeleted()) {
            throw new RuntimeException("Product is not deleted");
        }

        product.setDeleted(false);
        productRepository.save(product);
    }

    public List<ProductResponse> getDeletedProducts() {
        return productRepository.findByIsDeletedTrue()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

}