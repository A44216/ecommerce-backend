package com.ecommerce.backend.service.admin;

import com.ecommerce.backend.dto.requests.admin.category.AdminCategoryRequest;
import com.ecommerce.backend.dto.responses.admin.profile.CategoryAdminResponse;
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
                .orElseThrow(() -> new ResourceNotFoundException("CATEGORY_NOT_FOUND"));
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
    public CategoryAdminResponse createCategory(AdminCategoryRequest request) {

        String name = Objects.requireNonNull(request.getName()).trim();

        // check trùng trước
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("CATEGORY_ALREADY_EXISTS");
        }

        try {
            Category category = new Category();
            category.setName(name);

            return mapToAdminDTO(categoryRepository.save(category));

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // fallback chống race condition
            throw new BadRequestException("CATEGORY_ALREADY_EXISTS");
        }
    }

    // UPDATE
    @Transactional
    public CategoryAdminResponse updateCategory(Integer id, AdminCategoryRequest request) {

        Category category = getCategoryOrThrow(id);

        String name = Objects.requireNonNull(request.getName()).trim();

        boolean isDuplicate = categoryRepository
                .existsByNameIgnoreCaseAndIdNot(name, id);

        if (isDuplicate) {
            throw new BadRequestException("CATEGORY_ALREADY_EXISTS");
        }

        try {
            category.setName(name);
            return mapToAdminDTO(categoryRepository.save(category));

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new BadRequestException("CATEGORY_ALREADY_EXISTS");
        }
    }

    // DELETE (SOFT)
    @Transactional
    public void deleteCategory(Integer id) {

        Category category = getCategoryOrThrow(id);

        if (category.getIsDeleted()) {
            throw new BadRequestException("CATEGORY_ALREADY_DELETED");
        }

        category.setIsDeleted(true);
    }

    // RESTOR
    @Transactional
    public void restoreCategory(Integer id) {

        Category category = getCategoryOrThrow(id);

        if (!category.getIsDeleted()) {
            throw new BadRequestException("CATEGORY_NOT_DELETED");
        }

        category.setIsDeleted(false);
    }
}