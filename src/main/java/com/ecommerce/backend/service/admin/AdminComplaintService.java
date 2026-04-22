package com.ecommerce.backend.service.admin;

import com.ecommerce.backend.dto.responses.admin.complaint.AdminComplaintDetailResponse;
import com.ecommerce.backend.dto.responses.admin.complaint.AdminComplaintResponse;
import com.ecommerce.backend.dto.responses.admin.user.AdminUserResponse;
import com.ecommerce.backend.dto.responses.seller.PageResponse;
import com.ecommerce.backend.entity.Complaint;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.enums.ComplaintStatus;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminComplaintService {

    private final ComplaintRepository complaintRepository;

    public PageResponse<AdminComplaintResponse> getComplaints(int page, int size, ComplaintStatus status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Complaint> complaints = complaintRepository.adminSearchComplaints(status, pageable);

        return new PageResponse<>(
                complaints.getContent().stream().map(this::mapToDTO).toList(),
                complaints.getNumber(),
                complaints.getSize(),
                complaints.getTotalElements(),
                complaints.getTotalPages()
        );
    }

    public AdminComplaintDetailResponse getComplaintById(Integer id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));
        return mapToDetailDTO(complaint);
    }

    public void updateComplaintStatus(Integer id, ComplaintStatus status) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));

        complaint.setStatus(status);

        if (status == ComplaintStatus.RESOLVED || status == ComplaintStatus.REJECTED) {
            complaint.setResolvedAt(java.time.LocalDateTime.now());
        }

        complaintRepository.save(complaint);
    }

    private AdminComplaintResponse mapToDTO(Complaint complaint) {
        return AdminComplaintResponse.builder()
                .id(complaint.getId())
                .userId(complaint.getUser() != null ? complaint.getUser().getId() : null)
                .username(complaint.getUser() != null ? complaint.getUser().getUsername() : null)
                .orderId(complaint.getOrder() != null ? complaint.getOrder().getId() : null)
                .content(complaint.getContent())
                .status(complaint.getStatus())
                .createdAt(complaint.getCreatedAt())
                .build();
    }

    private AdminComplaintDetailResponse mapToDetailDTO(Complaint complaint) {
        return AdminComplaintDetailResponse.builder()
                .id(complaint.getId())
                .content(complaint.getContent())
                .status(complaint.getStatus())
                .createdAt(complaint.getCreatedAt())
                .resolvedAt(complaint.getResolvedAt())
                .user(mapUserToDTO(complaint.getUser()))
                .orderId(complaint.getOrder() != null ? complaint.getOrder().getId() : null)
                .orderTotal(complaint.getOrder() != null ? complaint.getOrder().getTotalPrice() : null)
                .build();
    }

    private AdminUserResponse mapUserToDTO(User user) {
        if (user == null) return null;
        return AdminUserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .avatar(user.getAvatar())
                .build();
    }
}
