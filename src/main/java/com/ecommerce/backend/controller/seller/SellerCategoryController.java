package com.ecommerce.backend.controller.seller;

import com.ecommerce.backend.dto.responses.seller.product.SellerCategoryResponse;
import com.ecommerce.backend.service.seller.SellerCategoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("hasRole('SELLER')")
@RestController
@RequestMapping("/api/seller/categories")
@CrossOrigin
public class SellerCategoryController {

    private final SellerCategoryService categoryService;

    public SellerCategoryController(SellerCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<SellerCategoryResponse> getCategories(
            @RequestParam(required = false) String keyword
    ) {
        return categoryService.getCategories(keyword);
    }

    @GetMapping("/{id}")
    public SellerCategoryResponse getCategoryById(@PathVariable Integer id) {
        return categoryService.getCategoryById(id);
    }
}