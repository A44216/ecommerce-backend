package com.ecommerce.backend.service.seller;

import com.ecommerce.backend.dto.requests.seller.product.SellerProductRequest;
import com.ecommerce.backend.dto.responses.ProductAutocompleteResponse;
import com.ecommerce.backend.dto.responses.ProductImageResponse;
import com.ecommerce.backend.dto.responses.seller.PageResponse;
import com.ecommerce.backend.dto.responses.seller.product.SellerProductResponse;
import com.ecommerce.backend.entity.Category;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.Shop;
import com.ecommerce.backend.enums.ProductStatus;
import com.ecommerce.backend.repository.CategoryRepository;
import com.ecommerce.backend.repository.ProductRepository;
import org.springframework.data.domain.*;
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

    // ENTITY -> DTO
    private SellerProductResponse mapToDTO(Product product) {
        return SellerProductResponse.builder()
                .id(product.getId())
                .productCode(product.getProductCode())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .description(product.getDescription())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .shopName(product.getShop() != null ? product.getShop().getShopName() : null)
                .ratingAvg(product.getRatingAvg())
                .ratingCount(product.getRatingCount())
                .soldCount(product.getSoldCount())
                .status(product.getStatus())
                .isDeleted(product.isDeleted())
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

    // REQUEST -> ENTITY
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

        Integer shopId = sellerShopService.getMyShop().getId();

        Shop shop = new Shop();
        shop.setId(shopId);
        product.setShop(shop);

        if (product.getId() == null) {
            product.setStatus(ProductStatus.PENDING);
        }
    }

    // GET DETAIL
    public SellerProductResponse getProductById(Integer id) {

        Integer shopId = sellerShopService.getMyShop().getId();

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getShop().getId().equals(shopId)) {
            throw new RuntimeException("Access denied");
        }

        return mapToDTO(product);
    }

    // CREATE
    public SellerProductResponse createProduct(SellerProductRequest request) {

        Product product = new Product();
        mapRequestToProduct(product, request);

        // save lần 1 để có ID
        product = productRepository.save(product);

        // set productCode
        product.setProductCode("PRD-" + String.format("%06d", product.getId()));

        // save lần 2
        product = productRepository.save(product);

        return mapToDTO(product);
    }

    // UPDATE
    public SellerProductResponse updateProduct(Integer id, SellerProductRequest request) {

        Integer shopId = sellerShopService.getMyShop().getId();

        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getShop().getId().equals(shopId)) {
            throw new RuntimeException("Access denied");
        }

        mapRequestToProduct(product, request);

        return mapToDTO(productRepository.save(product));
    }

    // SOFT DELETE
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

    // RESTORE
    public void restoreProduct(Integer id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.isDeleted()) {
            throw new RuntimeException("Product is not deleted");
        }

        product.setDeleted(false);
        productRepository.save(product);
    }

    // SUBMIT
    public void submitProduct(Integer id) {

        Integer shopId = sellerShopService.getMyShop().getId();

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getShop().getId().equals(shopId)) {
            throw new RuntimeException("Access denied");
        }

        if (product.getStatus() != ProductStatus.REJECTED
                && product.getStatus() != ProductStatus.PENDING) {
            throw new RuntimeException("Product cannot be submitted");
        }

        product.setStatus(ProductStatus.PENDING);
        productRepository.save(product);
    }

    // FILTER
    public PageResponse<SellerProductResponse> filterProducts(
            ProductStatus status,
            Boolean isDeleted,
            String keyword,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Integer shopId = sellerShopService.getMyShop().getId();

        Page<Product> products = productRepository.filterProducts(
                shopId,
                isDeleted,
                status,
                (keyword == null || keyword.isBlank()) ? null : keyword.trim(),
                pageable
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

    // CATEGORY
    public PageResponse<SellerProductResponse> getProductsByCategory(Integer categoryId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Integer shopId = sellerShopService.getMyShop().getId();

        Page<Product> products =
                productRepository.findByShopIdAndCategoryIdAndIsDeletedFalse(
                        shopId, categoryId, pageable
                );

        return new PageResponse<>(
                products.map(this::mapToDTO).getContent(),
                products.getNumber(),
                products.getSize(),
                products.getTotalElements(),
                products.getTotalPages()
        );
    }

    public List<ProductAutocompleteResponse> autocompleteProducts(String keyword) {

        String k = (keyword == null) ? "" : keyword.trim();
        if (k.isEmpty()) {
            return List.of();
        }

        Integer shopId = sellerShopService.getMyShop().getId();
        Pageable pageable = PageRequest.of(0, 5);

        return productRepository
                .autocompleteProducts(shopId, k, pageable)
                .getContent();
    }

}