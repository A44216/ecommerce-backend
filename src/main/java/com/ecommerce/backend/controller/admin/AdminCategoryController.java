package com.ecommerce.backend.controller.admin;

import com.ecommerce.backend.dto.requests.CategoryRequest;
import com.ecommerce.backend.dto.responses.admin.category.CategoryAdminResponse;
import com.ecommerce.backend.service.admin.AdminCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin/categories")
@CrossOrigin
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    public AdminCategoryController(AdminCategoryService adminCategoryService) {
        this.adminCategoryService = adminCategoryService;
    }

    @GetMapping
    public List<CategoryAdminResponse> getCategories(
            @RequestParam(required = false) Boolean isDeleted,
            @RequestParam(required = false) String keyword
    ) {
        return adminCategoryService.getCategories(isDeleted, keyword);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryAdminResponse create(@Valid @RequestBody CategoryRequest request) {
        return adminCategoryService.createCategory(request);
    }

    @PutMapping("/{id}")
    public CategoryAdminResponse update(
            @PathVariable Integer id,
            @Valid @RequestBody CategoryRequest request
    ) {
        return adminCategoryService.updateCategory(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        adminCategoryService.deleteCategory(id);
    }

    @PutMapping("/{id}/restore")
    public void restore(@PathVariable Integer id) {
        adminCategoryService.restoreCategory(id);
    }
}