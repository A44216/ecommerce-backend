package com.ecommerce.backend.service.seller;

import com.ecommerce.backend.dto.requests.seller.product.SellerProductRequest;
import com.ecommerce.backend.dto.responses.ProductImageResponse;
import com.ecommerce.backend.dto.responses.seller.PageResponse;
import com.ecommerce.backend.dto.responses.seller.product.SellerProductResponse;
import com.ecommerce.backend.entity.Category;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.Shop;
import com.ecommerce.backend.enums.ProductStatus;
import com.ecommerce.backend.repository.CategoryRepository;
import com.ecommerce.backend.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SellerProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SellerShopService sellerShopService;

    public SellerProductService(ProductRepository productRepository,
                                CategoryRepository categoryRepository,
                                SellerShopService sellerShopService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.sellerShopService = sellerShopService;
    }

    // ENTITY -> RESPONSE DTO
    private SellerProductResponse mapToDTO(Product product) {
        return SellerProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .description(product.getDescription())
                .categoryName(
                        product.getCategory() != null
                                ? product.getCategory().getName()
                                : null
                )
                .shopName(product.getShop().getShopName())
                .ratingAvg(product.getRatingAvg())
                .ratingCount(product.getRatingCount())
                .soldCount(product.getSoldCount())
                .status(product.getStatus())
                .images(
                        product.getImages() == null ? List.of() :
                                product.getImages().stream()
                                        .map(img -> ProductImageResponse.builder()
                                                .id(img.getId())
                                                .imageUrl(img.getImageUrl())
                                                .build())
                                        .toList()
                )
                .build();
    }

    // REQUEST DTO -> ENTITY
    private void mapRequestToProduct(Product product, SellerProductRequest request) {

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setDescription(request.getDescription());

        if (request.getCategoryId() != null) {

            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));

            product.setCategory(category);

        } else {
            product.setCategory(null);
        }

        // LẤY SHOP TỪ USER LOGIN
        Integer shopId = sellerShopService.getMyShop().getId();

        Shop shop = new Shop();
        shop.setId(shopId);

        product.setShop(shop);

        product.setStatus(ProductStatus.PENDING);
    }

    // lấy tất cả sản phẩm
    public PageResponse<SellerProductResponse> getAllProducts(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Integer shopId = sellerShopService.getMyShop().getId();

        Page<Product> products =
                productRepository.findByShopIdAndIsDeletedFalse(shopId, pageable);

        Page<SellerProductResponse> mapped = products.map(this::mapToDTO);

        return new PageResponse<>(
                mapped.getContent(),
                mapped.getNumber(),
                mapped.getSize(),
                mapped.getTotalElements(),
                mapped.getTotalPages()
        );
    }

    // lấy sản phẩm theo id
    public SellerProductResponse getProductById(Integer id) {

        Integer shopId = sellerShopService.getMyShop().getId();

        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getShop().getId().equals(shopId)) {
            throw new RuntimeException("Access denied");
        }

        return mapToDTO(product);
    }

    // thêm sản phẩm
    public SellerProductResponse createProduct(SellerProductRequest request) {

        Product product = new Product();

        mapRequestToProduct(product, request);

        Product saved = productRepository.save(product);

        return mapToDTO(saved);
    }

    // cập nhật sản phẩm
    public SellerProductResponse updateProduct(Integer id, SellerProductRequest request) {

        Integer shopId = sellerShopService.getMyShop().getId();

        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getShop().getId().equals(shopId)) {
            throw new RuntimeException("Access denied");
        }

        mapRequestToProduct(product, request);

        Product updated = productRepository.save(product);

        return mapToDTO(updated);
    }

    // xóa sản phẩm
    public void deleteProduct(Integer id) {

        Integer shopId = sellerShopService.getMyShop().getId();

        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getShop().getId().equals(shopId)) {
            throw new RuntimeException("Access denied");
        }

        product.setDeleted(true);

        productRepository.save(product);
    }

    // sản phẩm theo category
    public PageResponse<SellerProductResponse> getProductsByCategory(Integer categoryId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Integer shopId = sellerShopService.getMyShop().getId();

        Page<Product> products =
                productRepository.findByShopIdAndCategoryIdAndIsDeletedFalse(
                        shopId, categoryId, pageable
                );

        Page<SellerProductResponse> mapped = products.map(this::mapToDTO);

        return new PageResponse<>(
                mapped.getContent(),
                mapped.getNumber(),
                mapped.getSize(),
                mapped.getTotalElements(),
                mapped.getTotalPages()
        );
    }

    // sản phẩm theo shop
    public PageResponse<SellerProductResponse> getProductsByShop(Integer shopId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Product> products = productRepository.findByShopIdAndIsDeletedFalse(shopId, pageable);

        Page<SellerProductResponse> mapped = products.map(this::mapToDTO);

        return new PageResponse<>(
                mapped.getContent(),
                mapped.getNumber(),
                mapped.getSize(),
                mapped.getTotalElements(),
                mapped.getTotalPages()
        );
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

    public PageResponse<SellerProductResponse> getDeletedProducts(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Integer shopId = sellerShopService.getMyShop().getId();

        Page<Product> products =
                productRepository.findByShopIdAndIsDeletedTrue(shopId, pageable);

        Page<SellerProductResponse> mapped = products.map(this::mapToDTO);

        return new PageResponse<>(
                mapped.getContent(),
                mapped.getNumber(),
                mapped.getSize(),
                mapped.getTotalElements(),
                mapped.getTotalPages()
        );
    }

    public PageResponse<SellerProductResponse> searchProducts(String keyword, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Integer shopId = sellerShopService.getMyShop().getId();

        Page<Product> products;

        if (keyword != null && !keyword.trim().isEmpty()) {
            products = productRepository.searchByNameOrCategory(
                    shopId,
                    keyword.trim(),
                    pageable
            );
        } else {
            products = productRepository.findByShopIdAndIsDeletedFalse(shopId, pageable);
        }

        Page<SellerProductResponse> mapped = products.map(this::mapToDTO);

        return new PageResponse<>(
                mapped.getContent(),
                mapped.getNumber(),
                mapped.getSize(),
                mapped.getTotalElements(),
                mapped.getTotalPages()
        );
    }

}