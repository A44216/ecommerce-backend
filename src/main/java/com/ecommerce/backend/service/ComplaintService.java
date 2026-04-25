package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.ComplaintRequest;
import com.ecommerce.backend.dto.responses.ComplaintResponse;
import com.ecommerce.backend.entity.Complaint;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.enums.ComplaintStatus;
import com.ecommerce.backend.repository.ComplaintRepository;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public void submitComplaint(ComplaintRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Complaint complaint = new Complaint();
        complaint.setUser(user);
        complaint.setContent(request.getContent());
        complaint.setStatus(ComplaintStatus.PENDING);

        if (request.getOrderId() != null) {
            Order order = orderRepository.findById(request.getOrderId()).orElse(null);
            complaint.setOrder(order);
        }

        complaintRepository.save(complaint);
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
        res.setOrderId(c.getOrder() != null ? c.getOrder().getId() : null);
        res.setContent(c.getContent());
        res.setStatus(c.getStatus());
        res.setCreatedAt(c.getCreatedAt());
        return res;
    }
}