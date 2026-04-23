package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    // USER
    List<Category> findByIsDeletedFalseOrderByNameAsc();

    List<Category> findByNameContainingIgnoreCaseAndIsDeletedFalseOrderByNameAsc(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id);

    // ADMIN FILTER (CHUNG 1 HÀM)
    List<Category> findByIsDeletedAndNameContainingIgnoreCaseOrderByNameAsc(
            Boolean isDeleted,
            String name
    );

    // ADMIN AUTOCOMPLETE
    List<Category> findTop5ByNameContainingIgnoreCaseOrderByNameAsc(String name);
}