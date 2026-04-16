package com.ecommerce.backend.service.seller;

import com.ecommerce.backend.dto.responses.seller.category.SellerCategoryResponse;
import com.ecommerce.backend.entity.Category;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SellerCategoryService {

    private final CategoryRepository categoryRepository;

    public SellerCategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    private SellerCategoryResponse mapToDTO(Category category) {
        return new SellerCategoryResponse(
                category.getId(),
                category.getName()
        );
    }

    @Transactional(readOnly = true)
    public List<SellerCategoryResponse> getCategories(String keyword) {

        List<Category> categories;

        if (keyword == null || keyword.trim().isEmpty()) {
            categories = categoryRepository.findByIsDeletedFalseOrderByNameAsc();
        } else {
            categories = categoryRepository
                    .findByNameContainingIgnoreCaseAndIsDeletedFalseOrderByNameAsc(keyword.trim());
        }

        return categories.stream().map(this::mapToDTO).toList();
    }

    @Transactional(readOnly = true)
    public SellerCategoryResponse getCategoryById(Integer id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CATEGORY_NOT_FOUND"));

        if (category.getIsDeleted()) {
            throw new ResourceNotFoundException("CATEGORY_NOT_FOUND");
        }

        return mapToDTO(category);
    }
}