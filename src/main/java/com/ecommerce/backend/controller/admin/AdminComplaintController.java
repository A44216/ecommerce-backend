package com.ecommerce.backend.controller.admin;

import com.ecommerce.backend.dto.responses.admin.complaint.AdminComplaintDetailResponse;
import com.ecommerce.backend.dto.responses.admin.complaint.AdminComplaintResponse;
import com.ecommerce.backend.dto.responses.seller.PageResponse;
import com.ecommerce.backend.enums.ComplaintStatus;
import com.ecommerce.backend.service.admin.AdminComplaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin/complaints")
@RequiredArgsConstructor
public class AdminComplaintController {

    private final AdminComplaintService adminComplaintService;

    @GetMapping
    public ResponseEntity<PageResponse<AdminComplaintResponse>> getComplaints(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ComplaintStatus status) {
        return ResponseEntity.ok(adminComplaintService.getComplaints(page, size, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminComplaintDetailResponse> getComplaintById(@PathVariable Integer id) {
        return ResponseEntity.ok(adminComplaintService.getComplaintById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateComplaintStatus(
            @PathVariable Integer id,
            @RequestParam ComplaintStatus status) {
        adminComplaintService.updateComplaintStatus(id, status);
        return ResponseEntity.noContent().build();
    }
}
