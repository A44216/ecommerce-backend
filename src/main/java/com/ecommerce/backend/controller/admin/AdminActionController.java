package com.ecommerce.backend.controller.admin;

import com.ecommerce.backend.dto.responses.admin.action.AdminActionResponse;
import com.ecommerce.backend.dto.responses.seller.PageResponse;
import com.ecommerce.backend.enums.EntityType;
import com.ecommerce.backend.service.admin.AdminActionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin/actions")
@RequiredArgsConstructor
public class AdminActionController {

    private final AdminActionService adminActionService;

    @GetMapping
    public ResponseEntity<PageResponse<AdminActionResponse>> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) EntityType entityType
    ) {
        return ResponseEntity.ok(
                adminActionService.getLogs(page, size, entityType)
        );
    }
}