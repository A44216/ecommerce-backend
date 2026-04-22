package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.ComplaintRequest;
import com.ecommerce.backend.dto.responses.ComplaintResponse;
import com.ecommerce.backend.entity.Complaint;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.enums.ComplaintStatus;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.ComplaintRepository;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public ComplaintService(ComplaintRepository complaintRepository, UserRepository userRepository, OrderRepository orderRepository) {
        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    // MAPPER
    private ComplaintResponse mapToDTO(Complaint complaint) {
        return ComplaintResponse.builder()
                .id(complaint.getId())
                .userId(complaint.getUser().getId())
                .orderId(complaint.getOrder() != null ? complaint.getOrder().getId() : null)
                .content(complaint.getContent())
                .status(complaint.getStatus())
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

    // lấy khiếu nại theo Order
    public List<ComplaintResponse> getComplaintsByOrderId(Integer orderId) {
        return complaintRepository.findByOrderId(orderId).stream()
                .map(this::mapToDTO)
                .toList();
    }

    // tạo khiếu nại mới
    public ComplaintResponse createComplaint(ComplaintRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        Complaint complaint = new Complaint();
        complaint.setUser(user);
        complaint.setOrder(order);
        complaint.setContent(request.getContent());
        complaint.setStatus(ComplaintStatus.PENDING); // Mặc định là PENDING khi mới tạo

        return mapToDTO(complaintRepository.save(complaint));
    }

    // xóa khiếu nại
    public void deleteComplaint(Integer id) {
        if (!complaintRepository.existsById(id)) {
            throw new ResourceNotFoundException("Complaint not found");
        }
        complaintRepository.deleteById(id);
    }
}