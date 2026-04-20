package com.ecommerce.backend.controller.admin;

import com.ecommerce.backend.dto.requests.admin.platformfee.AdminPlatformFeeRequest;
import com.ecommerce.backend.dto.responses.admin.platformfee.AdminPlatformFeeResponse;
import com.ecommerce.backend.service.admin.AdminPlatformFeeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin/platform-fees")
public class AdminPlatformFeeController {

    private final AdminPlatformFeeService adminPlatformFeeService;

    public AdminPlatformFeeController(AdminPlatformFeeService adminPlatformFeeService) {
        this.adminPlatformFeeService = adminPlatformFeeService;
    }

    @GetMapping("/current")
    public ResponseEntity<AdminPlatformFeeResponse> getCurrentFee() {
        return ResponseEntity.ok(adminPlatformFeeService.getCurrentFee());
    }

    @PostMapping
    public ResponseEntity<AdminPlatformFeeResponse> updateCurrentFee(
            @Valid @RequestBody AdminPlatformFeeRequest request) {
        return ResponseEntity.ok(adminPlatformFeeService.updateCurrentFee(request));
    }
}
