package com.ecommerce.backend.controller.admin;

import com.ecommerce.backend.dto.requests.admin.complaint.AdminReplyComplaintRequest;
import com.ecommerce.backend.dto.responses.admin.complaint.AdminComplaintDetailResponse;
import com.ecommerce.backend.dto.responses.admin.complaint.AdminComplaintResponse;
import com.ecommerce.backend.dto.responses.seller.PageResponse;
import com.ecommerce.backend.enums.ComplaintStatus;
import com.ecommerce.backend.service.admin.AdminComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        return ResponseEntity.ok(
                adminComplaintService.getComplaints(
                        page, size,
                        status, keyword,
                        sortBy, direction
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminComplaintDetailResponse> getComplaintById(@PathVariable Integer id) {
        return ResponseEntity.ok(adminComplaintService.getComplaintById(id));
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<List<String>> autocomplete(
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(adminComplaintService.autocomplete(keyword));
    }

    @PostMapping("/{id}/reply")
    public ResponseEntity<Void> replyComplaint(
            @PathVariable Integer id,
            @RequestBody @Valid AdminReplyComplaintRequest request
    ) {
        adminComplaintService.replyComplaint(
                id,
                request.getStatus(),
                request.getResponse()
        );

        return ResponseEntity.noContent().build();
    }


}
