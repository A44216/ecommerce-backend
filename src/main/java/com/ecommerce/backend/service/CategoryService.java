package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.CategoryRequest;
import com.ecommerce.backend.dto.responses.CategoryResponse;
import com.ecommerce.backend.entity.Category;
import com.ecommerce.backend.exception.BadRequestException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    private CategoryResponse mapToDTO(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName()
        );
    }

    private Category getCategoryOrThrow(Integer id) {
        return categoryRepository.findActiveById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found or deleted with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllActive()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Integer id) {
        return mapToDTO(getCategoryOrThrow(id));
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {

        String name = request.getName().trim();

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("Category already exists");
        }

        Category category = new Category();
        category.setName(name);

        return mapToDTO(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse updateCategory(Integer id, CategoryRequest request) {

        Category category = getCategoryOrThrow(id);

        String name = request.getName().trim();

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new BadRequestException("Category name already exists");
        }

        category.setName(name);

        return mapToDTO(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Integer id) {
        categoryRepository.delete(getCategoryOrThrow(id));
    }
}