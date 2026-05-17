package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.ComplaintRequest;
import com.ecommerce.backend.dto.responses.ComplaintResponse;
import com.ecommerce.backend.entity.Complaint;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.enums.ComplaintStatus;

import com.ecommerce.backend.enums.NotificationType;
import com.ecommerce.backend.enums.Role;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.ComplaintRepository;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    public ComplaintService(ComplaintRepository complaintRepository, UserRepository userRepository, 
                            OrderRepository orderRepository, NotificationService notificationService) {
        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
    }

    // MAPPER
    private ComplaintResponse mapToDTO(Complaint complaint) {
        return ComplaintResponse.builder()
                .id(complaint.getId())
                .userId(complaint.getUser().getId())
                .content(complaint.getContent())
                .status(complaint.getStatus())
                .complaintCode(complaint.getComplaintCode())
                .createdAt(complaint.getCreatedAt())
                .resolvedAt(complaint.getResolvedAt())
                .build();
    }

    // lấy tất cả khiếu nại (Dành cho Admin)
    public List<ComplaintResponse> getAllComplaints() {
        return complaintRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    // lấy chi tiết 1 khiếu nại
    public ComplaintResponse getComplaintById(Integer id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));
        return mapToDTO(complaint);
    }

    // lấy khiếu nại theo User
    public List<ComplaintResponse> getComplaintsByUserId(Integer userId) {
        return complaintRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .toList();
    }

    // tạo khiếu nại mới
    @Transactional
    public ComplaintResponse createComplaint(ComplaintRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Complaint complaint = new Complaint();
        complaint.setUser(user);
        
        complaint.setContent(request.getContent());
        complaint.setStatus(ComplaintStatus.PENDING); // Mặc định là PENDING khi mới tạo
        complaint.setComplaintCode(generateComplaintCode());

        Complaint savedComplaint = complaintRepository.save(complaint);
        
        // Gửi thông báo cho Admin
        notifyAdmins(savedComplaint);

        return mapToDTO(savedComplaint);
    }

    private void notifyAdmins(Complaint complaint) {
        List<User> admins = userRepository.findByRole(Role.ADMIN);
        String title = "Khiếu nại mới từ khách hàng";
        String body = "Bạn có một khiếu nại mới cần xử lý: " + complaint.getComplaintCode();
        
        for (User admin : admins) {
            notificationService.createNotification(
                    admin.getId(),
                    title,
                    body,
                    NotificationType.COMPLAINT,
                    complaint.getId()
            );
        }
    }

    // xóa khiếu nại
    @Transactional
    public void deleteComplaint(Integer id) {
        if (!complaintRepository.existsById(id)) {
            throw new ResourceNotFoundException("Complaint not found");
        }
        complaintRepository.deleteById(id);
    }

    @Transactional
    public void submitComplaint(ComplaintRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Complaint complaint = new Complaint();
        complaint.setUser(user);
        complaint.setContent(request.getContent());
        complaint.setStatus(ComplaintStatus.PENDING);
        complaint.setComplaintCode(generateComplaintCode());

        Complaint savedComplaint = complaintRepository.save(complaint);
        
        // Gửi thông báo cho Admin
        notifyAdmins(savedComplaint);
    }

    public List<ComplaintResponse> getComplaintsByUser(Integer userId) {
        return complaintRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ComplaintResponse mapToResponse(Complaint c) {
        ComplaintResponse res = new ComplaintResponse();
        res.setId(c.getId());
        res.setContent(c.getContent());
        res.setStatus(c.getStatus());
        res.setComplaintCode(c.getComplaintCode());
        res.setCreatedAt(c.getCreatedAt());
        return res;

    }

    private String generateComplaintCode() {
        return "CMP-" + System.currentTimeMillis() + "-" + java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}