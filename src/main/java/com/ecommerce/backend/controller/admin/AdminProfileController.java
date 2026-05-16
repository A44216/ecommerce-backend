package com.ecommerce.backend.controller.admin;

import com.ecommerce.backend.dto.requests.admin.profile.AdminProfileInfoRequest;
import com.ecommerce.backend.dto.responses.admin.profile.AdminProfileInfoResponse;
import com.ecommerce.backend.service.admin.AdminProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin/profile")
@RequiredArgsConstructor
public class AdminProfileController {

    private final AdminProfileService adminProfileService;

    @GetMapping
    public AdminProfileInfoResponse getProfile(Authentication authentication) {
        return adminProfileService.getProfile(authentication);
    }

    @PutMapping
    public AdminProfileInfoResponse updateProfile(
            Authentication authentication,
            @Valid @RequestBody AdminProfileInfoRequest request
    ) {
        return adminProfileService.updateProfile(authentication, request);
    }
}