package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.requests.CategoryRequest;
import com.ecommerce.backend.dto.responses.CategoryResponse;
import com.ecommerce.backend.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // tất cả category
    @GetMapping
    public List<CategoryResponse> getAllCategories() {
        return categoryService.getAllCategories();
    }

    // category theo id
    @GetMapping("/{id}")
    public CategoryResponse getCategoryById(@PathVariable Integer id) {
        return categoryService.getCategoryById(id);
    }

    // thêm category
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createCategory(@Valid @RequestBody CategoryRequest request) {
        return categoryService.createCategory(request);
    }

    // cập nhật category
    @PutMapping("/{id}")
    public CategoryResponse updateCategory(
            @PathVariable Integer id,
            @Valid @RequestBody CategoryRequest request) {

        return categoryService.updateCategory(id, request);
    }

    // xoá category
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Integer id) {
        categoryService.deleteCategory(id);
    }
}