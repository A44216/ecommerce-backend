package com.ecommerce.backend.controller.admin;

import com.ecommerce.backend.dto.responses.seller.PageResponse;
import com.ecommerce.backend.dto.responses.admin.user.AdminUserDetailResponse;
import com.ecommerce.backend.dto.responses.admin.user.AdminUserResponse;
import com.ecommerce.backend.enums.Role;
import com.ecommerce.backend.enums.UserStatus;
import com.ecommerce.backend.service.admin.AdminUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    // LIST
    @GetMapping
    public ResponseEntity<PageResponse<AdminUserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(
                adminUserService.getUsers(page, size, status, keyword));
    }

    // DETAIL
    @GetMapping("/{id}")
    public ResponseEntity<AdminUserDetailResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(adminUserService.getUserById(id));
    }

    // CHANGE STATUS
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> changeStatus(
            @PathVariable Integer id,
            @RequestParam UserStatus status) {
        adminUserService.changeStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    // UPDATE ROLE
    @PatchMapping("/{id}/role")
    public ResponseEntity<Void> updateRole(
            @PathVariable Integer id,
            @RequestParam Role role) {
        adminUserService.updateRole(id, role);
        return ResponseEntity.noContent().build();
    }
}
