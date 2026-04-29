package com.ecommerce.backend.service.admin;

import com.ecommerce.backend.dto.responses.ProductAutocompleteResponse;
import com.ecommerce.backend.dto.responses.admin.product.AdminProductDetailResponse;
import com.ecommerce.backend.dto.responses.admin.product.AdminProductResponse;
import com.ecommerce.backend.dto.responses.seller.PageResponse;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.ProductImage;
import com.ecommerce.backend.enums.ProductStatus;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductRepository productRepository;

    public PageResponse<AdminProductResponse> getProducts(
            int page,
            int size,
            Integer shopId,
            Integer categoryId,
            ProductStatus status,
            Boolean isDeleted,
            String keyword,
            String sortBy,
            String direction
    ) {

        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "createdAt";
        }

        Sort sort;

        switch (sortBy) {

            case "createdAt":
                sort = "asc".equalsIgnoreCase(direction)
                        ? Sort.by("createdAt").ascending()
                        : Sort.by("createdAt").descending();
                break;

            case "price":
                sort = "asc".equalsIgnoreCase(direction)
                        ? Sort.by("price").ascending()
                        : Sort.by("price").descending();
                break;

            case "soldCount":
                sort = "asc".equalsIgnoreCase(direction)
                        ? Sort.by("soldCount").ascending()
                        : Sort.by("soldCount").descending();
                break;

            case "name":
                sort = "asc".equalsIgnoreCase(direction)
                        ? Sort.by("name").ascending()
                        : Sort.by("name").descending();
                break;

            default:
                sort = Sort.by("createdAt").descending();
                break;
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> products = productRepository.adminSearchProducts(
                shopId,
                categoryId,
                status,
                keyword,
                isDeleted,
                pageable
        );

        return new PageResponse<>(
                products.getContent().stream().map(this::mapToDTO).toList(),
                products.getNumber(),
                products.getSize(),
                products.getTotalElements(),
                products.getTotalPages()
        );
    }

    public AdminProductDetailResponse getProductById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return mapToDetailDTO(product);
    }

    private AdminProductResponse mapToDTO(Product product) {

        String image = (product.getImages() != null && !product.getImages().isEmpty())
                ? product.getImages().getFirst().getImageUrl()
                : null;

        return AdminProductResponse.builder()
                .id(product.getId())
                .productCode(product.getProductCode())
                .shopId(product.getShop() != null ? product.getShop().getId() : null)
                .shopName(product.getShop() != null ? product.getShop().getShopName() : null)
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .status(product.getStatus())
                .soldCount(product.getSoldCount())
                .createdAt(product.getCreatedAt())
                .image(image)
                .build();
    }

    private AdminProductDetailResponse mapToDetailDTO(Product product) {
        List<String> imageUrls = product.getImages() != null ?
                product.getImages().stream().map(ProductImage::getImageUrl).toList() : List.of();

        return AdminProductDetailResponse.builder()
                .id(product.getId())
                .productCode(product.getProductCode())
                .shopId(product.getShop() != null ? product.getShop().getId() : null)
                .shopName(product.getShop() != null ? product.getShop().getShopName() : null)
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .description(product.getDescription())
                .status(product.getStatus())
                .ratingAvg(product.getRatingAvg())
                .ratingCount(product.getRatingCount())
                .soldCount(product.getSoldCount())
                .createdAt(product.getCreatedAt())
                .images(imageUrls)
                .build();
    }

    public List<ProductAutocompleteResponse> autocomplete(String keyword, Integer shopId) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        Pageable pageable = PageRequest.of(0, 5);

        return productRepository.autocompleteAdminProducts(
                keyword.trim(),
                shopId,
                pageable
        );
    }

    public void updateStatus(Integer id, ProductStatus status) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // rule nghiệp vụ
        if (product.getStatus() == ProductStatus.REJECTED
                && status == ProductStatus.APPROVED) {
            throw new IllegalStateException("Cannot approve rejected product directly");
        }

        product.setStatus(status);
        productRepository.save(product);
    }

    public void deleteProduct(Integer id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // soft delete
        product.setDeleted(true);

        productRepository.save(product);
    }

    public void restoreProduct(Integer id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        product.setDeleted(false);

        productRepository.save(product);
    }

}
