package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.ProductImageRequest;
import com.ecommerce.backend.dto.responses.ProductImageResponse;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.ProductImage;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.ProductImageRepository;
import com.ecommerce.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;

    public ProductImageService(ProductImageRepository productImageRepository, ProductRepository productRepository) {
        this.productImageRepository = productImageRepository;
        this.productRepository = productRepository;
    }

    // MAPPER
    private ProductImageResponse mapToDTO(ProductImage productImage) {
        return ProductImageResponse.builder()
                .id(productImage.getId())
                .imageUrl(productImage.getImageUrl())
                .build();
    }

    // Lấy danh sách ảnh của 1 sản phẩm
    public List<ProductImageResponse> getImagesByProductId(Integer productId) {
        return productImageRepository.findByProductId(productId).stream()
                .map(this::mapToDTO)
                .toList();
    }

    // Thêm ảnh mới cho sản phẩm
    @Transactional
    public ProductImageResponse addProductImage(ProductImageRequest request) {
        // Kiểm tra xem sản phẩm có tồn tại không
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        ProductImage productImage = new ProductImage();
        productImage.setProduct(product);
        productImage.setImageUrl(request.getImageUrl());

        return mapToDTO(productImageRepository.save(productImage));
    }

    // Xóa ảnh
    @Transactional
    public void deleteProductImage(Integer id) {
        if (!productImageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product image not found");
        }
        productImageRepository.deleteById(id);
    }
}