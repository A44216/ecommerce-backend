package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.requests.ComplaintRequest;
import com.ecommerce.backend.dto.responses.ComplaintResponse;
import com.ecommerce.backend.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping
    public ResponseEntity<?> submitComplaint(@RequestBody ComplaintRequest request) {
        complaintService.submitComplaint(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    public List<ComplaintResponse> getMyComplaints(@PathVariable Integer userId) {
        return complaintService.getComplaintsByUser(userId);
    }
}