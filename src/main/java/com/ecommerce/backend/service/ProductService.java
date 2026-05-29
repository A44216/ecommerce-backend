package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.ProductRequest;
import com.ecommerce.backend.dto.responses.ProductImageResponse;
import com.ecommerce.backend.dto.responses.ProductResponse;
import com.ecommerce.backend.entity.Category;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.ProductImage;
import com.ecommerce.backend.entity.Shop;
import com.ecommerce.backend.enums.ProductStatus;
import com.ecommerce.backend.repository.CategoryRepository;
import com.ecommerce.backend.repository.ProductRepository;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    // ENTITY -> RESPONSE DTO
    private ProductResponse mapToDTO(Product product) {
        return ProductResponse.builder()
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
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .shopId(product.getShop() != null ? product.getShop().getId() : null)
                .shopName(product.getShop() != null ? product.getShop().getShopName() : null)
                .shopOwnerId((product.getShop() != null && product.getShop().getUser() != null) ? product.getShop().getUser().getId() : null)
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
    private void mapRequestToProduct(Product product, ProductRequest request) {

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

        Shop shop = new Shop();
        shop.setId(request.getShopId());
        product.setShop(shop);

        // Đã xóa hàm calculateStatus(stock) ở đây
    }

    // ==========================================
    // CÁC HÀM GET DÀNH CHO USER (CHỈ LẤY APPROVED)
    // ==========================================

    public List<ProductResponse> getAllProducts() {
        // Chỉ lấy sản phẩm APPROVED
        List<Product> products = productRepository.findByStatusAndIsDeletedFalse(ProductStatus.APPROVED);
        return products.stream().map(this::mapToDTO).toList();
    }

    public com.ecommerce.backend.dto.responses.PageResponse<ProductResponse> getAllProductsPaginated(int page, int size, String sortBy) {
        org.springframework.data.domain.Pageable pageable = createPageable(page, size, sortBy);
        org.springframework.data.domain.Page<Product> productPage = productRepository.findByStatusAndIsDeletedFalse(ProductStatus.APPROVED, pageable);

        List<ProductResponse> content = productPage.getContent().stream().map(this::mapToDTO).toList();

        return new com.ecommerce.backend.dto.responses.PageResponse<>(
                content,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast()
        );
    }

    public com.ecommerce.backend.dto.responses.PageResponse<ProductResponse> searchProductsPaginated(String keyword, int page, int size, String sortBy) {
        org.springframework.data.domain.Pageable pageable = createPageable(page, size, sortBy);
        org.springframework.data.domain.Page<Product> productPage = productRepository.findByNameContainingIgnoreCaseAndStatusAndIsDeletedFalse(keyword, ProductStatus.APPROVED, pageable);

        List<ProductResponse> content = productPage.getContent().stream().map(this::mapToDTO).toList();

        return new com.ecommerce.backend.dto.responses.PageResponse<>(
                content,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast()
        );
    }

    public com.ecommerce.backend.dto.responses.PageResponse<ProductResponse> getProductsByCategoryPaginated(Integer categoryId, int page, int size, String sortBy) {
        org.springframework.data.domain.Pageable pageable = createPageable(page, size, sortBy);
        org.springframework.data.domain.Page<Product> productPage = productRepository.findByCategoryIdAndStatusAndIsDeletedFalse(categoryId, ProductStatus.APPROVED, pageable);

        List<ProductResponse> content = productPage.getContent().stream().map(this::mapToDTO).toList();

        return new com.ecommerce.backend.dto.responses.PageResponse<>(
                content,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast()
        );
    }

    private org.springframework.data.domain.Pageable createPageable(int page, int size, String sortBy) {
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.unsorted();
        if (sortBy != null && !sortBy.isEmpty()) {
            String[] parts = sortBy.split(",");
            String property = parts[0];
            String direction = parts.length > 1 ? parts[1] : "asc";

            if (direction.equalsIgnoreCase("desc")) {
                sort = org.springframework.data.domain.Sort.by(property).descending();
            } else {
                sort = org.springframework.data.domain.Sort.by(property).ascending();
            }
        } else {
            // Default sort by ID descending (newest first) if no sort specified
            sort = org.springframework.data.domain.Sort.by("id").descending();
        }
        return org.springframework.data.domain.PageRequest.of(page, size, sort);
    }

    public ProductResponse getProductById(Integer id) {
        // Khách hàng chỉ xem được chi tiết nếu sản phẩm đó APPROVED
        Product product = productRepository.findByIdAndStatusAndIsDeletedFalse(id, ProductStatus.APPROVED)
                .orElseThrow(() -> new RuntimeException("Product not found or not approved"));
        return mapToDTO(product);
    }

    public List<ProductResponse> searchProducts(String keyword) {
        List<Product> products = productRepository.findByNameContainingIgnoreCaseAndStatusAndIsDeletedFalse(keyword, ProductStatus.APPROVED);
        return products.stream().map(this::mapToDTO).toList();
    }

    public List<String> suggestProductNames(String keyword) {
        List<Product> products = productRepository.findTop10ByNameContainingIgnoreCaseAndStatusAndIsDeletedFalse(keyword, ProductStatus.APPROVED);
        return products.stream()
                .map(Product::getName)
                .distinct()
                .toList();
    }

    public List<ProductResponse> getTrendingProducts() {
        List<Product> products = productRepository.findTop10ByStatusAndIsDeletedFalseOrderBySoldCountDesc(ProductStatus.APPROVED);
        return products.stream().map(this::mapToDTO).toList();
    }

    public List<ProductResponse> getProductsByCategory(Integer categoryId) {
        List<Product> products = productRepository.findByCategoryIdAndStatusAndIsDeletedFalse(categoryId, ProductStatus.APPROVED);
        return products.stream().map(this::mapToDTO).toList();
    }

    public List<ProductResponse> getProductsByShop(Integer shopId) {
        List<Product> products = productRepository.findByShopIdAndStatusAndIsDeletedFalse(shopId, ProductStatus.APPROVED);
        return products.stream().map(this::mapToDTO).toList();
    }

    public List<ProductResponse> searchProducts(String keyword, Integer shopId) {
        List<Product> products = (shopId != null)
                ? productRepository.findByShopIdAndStatusAndIsDeletedFalse(shopId, ProductStatus.APPROVED)
                : productRepository.findByStatusAndIsDeletedFalse(ProductStatus.APPROVED);

        if (keyword != null && !keyword.trim().isEmpty()) {
            String k = keyword.trim().toLowerCase();
            products = products.stream()
                    .filter(p ->
                            (p.getName() != null && p.getName().toLowerCase().contains(k))
                                    || (p.getCategory() != null
                                    && p.getCategory().getName().toLowerCase().contains(k))
                    )
                    .toList();
        }

        return products.stream().map(this::mapToDTO).toList();
    }

    // ==========================================
    // CÁC HÀM THAO TÁC DATA (THÊM, SỬA, XÓA, KHÔI PHỤC)
    // ==========================================

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();
        mapRequestToProduct(product, request);

        // MẶC ĐỊNH: Tạo mới phải chờ duyệt
        product.setStatus(ProductStatus.PENDING);

        Product saved = productRepository.save(product);
        return mapToDTO(saved);
    }

    @Transactional
    public ProductResponse updateProduct(Integer id, ProductRequest request) {
        // Dùng hàm cũ để Seller có thể lấy ra sửa dù sản phẩm đang bị PENDING hay REJECTED
        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        mapRequestToProduct(product, request);

        // MẶC ĐỊNH: Sửa thông tin xong phải chờ Admin duyệt lại
        product.setStatus(ProductStatus.PENDING);

        Product updated = productRepository.save(product);
        return mapToDTO(updated);
    }

    @Transactional
    public void deleteProduct(Integer id) {
        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setIsDeleted(true);
        productRepository.save(product);
    }

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

    public List<ProductResponse> getDeletedProducts() {
        return productRepository.findByIsDeletedTrue()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }
}