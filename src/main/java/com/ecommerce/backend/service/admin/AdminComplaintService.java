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
import com.ecommerce.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;

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
            complaint.setResolvedAt(LocalDateTime.now());
        }

        complaintRepository.save(complaint);
    }

    public void replyComplaint(Integer id, String response) {

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));

        User adminUser = getCurrentUser();

        complaint.setAdminResponse(response);
        complaint.setResolvedBy(adminUser);
        complaint.setResolvedAt(LocalDateTime.now());
        complaint.setStatus(ComplaintStatus.RESOLVED);

        complaintRepository.save(complaint);
    }

    private User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated user");
        }

        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
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
                .resolvedBy(complaint.getResolvedBy() != null ? complaint.getResolvedBy().getId() : null)
                .adminResponse(complaint.getAdminResponse())
                .build();
    }

    private AdminComplaintDetailResponse mapToDetailDTO(Complaint complaint) {
        return AdminComplaintDetailResponse.builder()
                .id(complaint.getId())
                .content(complaint.getContent())
                .status(complaint.getStatus())
                .createdAt(complaint.getCreatedAt())
                .resolvedAt(complaint.getResolvedAt())
                .resolvedBy(mapUserToDTO(complaint.getResolvedBy()))
                .adminResponse(complaint.getAdminResponse())
                .user(mapUserToDTO(complaint.getUser()))
                .orderId(complaint.getOrder() != null ? complaint.getOrder().getId() : null)
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
