package com.ecommerce.backend.service.admin;

import com.ecommerce.backend.dto.requests.CategoryRequest;
import com.ecommerce.backend.dto.responses.admin.category.CategoryAdminResponse;
import com.ecommerce.backend.entity.Category;
import com.ecommerce.backend.exception.BadRequestException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class AdminCategoryService {

    private final CategoryRepository categoryRepository;

    public AdminCategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // MAP
    private CategoryAdminResponse mapToAdminDTO(Category category) {
        return new CategoryAdminResponse(
                category.getId(),
                category.getName(),
                category.getIsDeleted()
        );
    }

    private Category getCategoryOrThrow(Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found with id: " + id));
    }

    // (FILTER + SEARCH)
    @Transactional(readOnly = true)
    public List<CategoryAdminResponse> getCategories(Boolean isDeleted, String keyword) {

        boolean deleted = Boolean.TRUE.equals(isDeleted);
        String k = (keyword == null) ? "" : keyword.trim();

        List<Category> categories =
                categoryRepository.findByIsDeletedAndNameContainingIgnoreCaseOrderByNameAsc(deleted, k);

        return categories.stream()
                .map(this::mapToAdminDTO)
                .toList();
    }

    // CREATE
    @Transactional
    public CategoryAdminResponse createCategory(CategoryRequest request) {

        String name = Objects.requireNonNull(request.getName()).trim();

        if (categoryRepository.existsByNameIgnoreCaseAndIsDeletedFalse(name)) {
            throw new BadRequestException("Category already exists");
        }

        Category category = new Category();
        category.setName(name);

        return mapToAdminDTO(categoryRepository.save(category));
    }

    // UPDATE
    @Transactional
    public CategoryAdminResponse updateCategory(Integer id, CategoryRequest request) {

        Category category = getCategoryOrThrow(id);

        String name = Objects.requireNonNull(request.getName()).trim();

        if (categoryRepository.existsByNameIgnoreCaseAndIdNotAndIsDeletedFalse(name, id)) {
            throw new BadRequestException("Category name already exists");
        }

        category.setName(name);

        return mapToAdminDTO(categoryRepository.save(category));
    }

    // DELETE (SOFT)
    @Transactional
    public void deleteCategory(Integer id) {

        Category category = getCategoryOrThrow(id);

        if (category.getIsDeleted()) {
            throw new BadRequestException("Category already deleted");
        }

        category.setIsDeleted(true);
    }

    // RESTOR
    @Transactional
    public void restoreCategory(Integer id) {

        Category category = getCategoryOrThrow(id);

        if (!category.getIsDeleted()) {
            throw new BadRequestException("Category is not deleted");
        }

        category.setIsDeleted(false);
    }
}