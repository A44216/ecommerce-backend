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
import com.ecommerce.backend.enums.ShopStatus;
import com.ecommerce.backend.repository.CategoryRepository;
import com.ecommerce.backend.repository.ProductRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SellerProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SellerShopService sellerShopService;
    private final com.ecommerce.backend.repository.UserRepository userRepository;
    private final com.ecommerce.backend.service.NotificationService notificationService;

    public SellerProductService(ProductRepository productRepository,
                                CategoryRepository categoryRepository,
                                SellerShopService sellerShopService,
                                com.ecommerce.backend.repository.UserRepository userRepository,
                                com.ecommerce.backend.service.NotificationService notificationService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.sellerShopService = sellerShopService;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
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
                .isDeleted(product.getIsDeleted())
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

        Shop shop = sellerShopService.getMyShopEntity();
        if (shop.getStatus() == ShopStatus.BLOCKED) {
            throw new RuntimeException("Shop has been locked by Admin. Cannot add or edit products.");
        }
        product.setShop(shop);

        if (product.getId() == null) {
            product.setStatus(ProductStatus.PENDING);
        }
    }

    // GET DETAIL
    public SellerProductResponse getProductById(Integer id) {

        Integer shopId = sellerShopService.getMyShopEntity().getId();

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getShop().getId().equals(shopId)) {
            throw new RuntimeException("Access denied");
        }

        return mapToDTO(product);
    }

    // CREATE
    @Transactional
    public SellerProductResponse createProduct(SellerProductRequest request) {

        Product product = new Product();
        mapRequestToProduct(product, request);

        // 1. Gán một mã ngẫu nhiên tạm thời để vượt qua lỗi NOT NULL
        product.setProductCode("TEMP-" + java.util.UUID.randomUUID().toString().substring(0, 8));

        // 2. save lần 1 để có ID
        product = productRepository.save(product);

        // 3. set productCode chính thức dựa vào ID vừa có
        product.setProductCode("PRD-" + String.format("%06d", product.getId()));

        // 4. save lần 2
        product = productRepository.save(product);

        // 5. Gửi thông báo cho toàn bộ Admin
        java.util.List<com.ecommerce.backend.entity.User> admins = userRepository.findByRole(com.ecommerce.backend.enums.Role.ADMIN);
        for (com.ecommerce.backend.entity.User admin : admins) {
            notificationService.createNotification(
                    admin.getId(),
                    "Sản phẩm mới chờ duyệt",
                    "Gian hàng " + product.getShop().getShopName() + " vừa tạo sản phẩm mới '" + product.getName() + "'. Vui lòng kiểm tra và phê duyệt.",
                    com.ecommerce.backend.enums.NotificationType.SYSTEM,
                    product.getId()
            );
        }

        return mapToDTO(product);
    }

    // UPDATE
    @Transactional
    public SellerProductResponse updateProduct(Integer id, SellerProductRequest request) {

        Integer shopId = sellerShopService.getMyShopEntity().getId();

        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getShop().getId().equals(shopId)) {
            throw new RuntimeException("Access denied");
        }

        mapRequestToProduct(product, request);

        return mapToDTO(productRepository.save(product));
    }

    // SOFT DELETE
    @Transactional
    public void deleteProduct(Integer id) {

        Integer shopId = sellerShopService.getMyShopEntity().getId();

        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getShop().getId().equals(shopId)) {
            throw new RuntimeException("Access denied");
        }

        product.setIsDeleted(true);
        productRepository.save(product);
    }

    // RESTORE
    @Transactional
    public void restoreProduct(Integer id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getIsDeleted()) {
            throw new RuntimeException("Product is not deleted");
        }

        product.setIsDeleted(false);
        productRepository.save(product);
    }

    // SUBMIT
    @Transactional
    public void submitProduct(Integer id) {

        Integer shopId = sellerShopService.getMyShopEntity().getId();

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

        Integer shopId = sellerShopService.getMyShopEntity().getId();

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

        Integer shopId = sellerShopService.getMyShopEntity().getId();

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

        Integer shopId = sellerShopService.getMyShopEntity().getId();
        Pageable pageable = PageRequest.of(0, 5);

        return productRepository
                .autocompleteProducts(shopId, k, pageable)
                .getContent();
    }

}