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
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;

    public PageResponse<AdminComplaintResponse> getComplaints(
            int page,
            int size,
            ComplaintStatus status,
            String keyword,
            String sortBy,
            String direction
    ) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Complaint> complaints =
                complaintRepository.adminSearchComplaints(
                        status,
                        keyword,
                        pageable
                );

        return new PageResponse<>(
                complaints.getContent()
                        .stream()
                        .map(this::mapToDTO)
                        .toList(),
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
                .complaintCode(complaint.getComplaintCode())
                .username(complaint.getUser() != null ? complaint.getUser().getUsername() : null)
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
                .complaintCode(complaint.getComplaintCode())
                .content(complaint.getContent())
                .status(complaint.getStatus())
                .createdAt(complaint.getCreatedAt())
                .resolvedAt(complaint.getResolvedAt())
                .resolvedBy(mapUserToDTO(complaint.getResolvedBy()))
                .adminResponse(complaint.getAdminResponse())
                .user(mapUserToDTO(complaint.getUser()))
                .build();
    }

    private AdminUserResponse mapUserToDTO(User user) {
        if (user == null) return null;
        return AdminUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .avatar(user.getAvatar())
                .build();
    }

    public List<String> autocomplete(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        return complaintRepository.autocompleteComplaints(keyword);
    }

    public void replyComplaint(Integer id, ComplaintStatus status, String response) {

        if (status != ComplaintStatus.RESOLVED && status != ComplaintStatus.REJECTED) {
            throw new IllegalArgumentException("Invalid final status");
        }

        if (response == null || response.trim().isEmpty()) {
            throw new IllegalArgumentException("Admin response is required");
        }

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));

        if (complaint.getStatus() != ComplaintStatus.PENDING) {
            throw new IllegalStateException("Complaint already processed");
        }

        User adminUser = getCurrentUser();

        complaint.setStatus(status);
        complaint.setAdminResponse(response.trim());
        complaint.setResolvedBy(adminUser);
        complaint.setResolvedAt(LocalDateTime.now());

        complaintRepository.save(complaint);
    }

}
