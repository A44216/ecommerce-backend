package com.ecommerce.backend.repository;

import com.ecommerce.backend.dto.responses.ProductResponse;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.enums.ProductStatus; // Nhớ import Enum này
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    // --- CÁC HÀM CŨ DÙNG CHO ADMIN / SELLER (Lấy tất cả không quan tâm trạng thái duyệt) ---
    List<Product> findByIsDeletedFalse();
    Optional<Product> findByIdAndIsDeletedFalse(Integer id);
    List<Product> findByShopIdAndIsDeletedFalse(Integer shopId);
    List<Product> findByIsDeletedTrue();

    // =================================================================================
    // --- CÁC HÀM MỚI DÀNH CHO USER (Khách hàng chỉ thấy sản phẩm có status cụ thể) ---
    // =================================================================================

    List<Product> findByStatusAndIsDeletedFalse(ProductStatus status);

    Optional<Product> findByIdAndStatusAndIsDeletedFalse(Integer id, ProductStatus status);

    List<Product> findByNameContainingIgnoreCaseAndStatusAndIsDeletedFalse(String keyword, ProductStatus status);

    List<Product> findByCategoryIdAndStatusAndIsDeletedFalse(Integer categoryId, ProductStatus status);

    List<Product> findByShopIdAndStatusAndIsDeletedFalse(Integer shopId, ProductStatus status);
}