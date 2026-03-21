package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.OrderRequest;
import com.ecommerce.backend.dto.responses.OrderResponse;
import com.ecommerce.backend.entity.Address;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.enums.OrderStatus;
import com.ecommerce.backend.exception.BadRequestException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.AddressRepository;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    public OrderService(OrderRepository repository,
                        UserRepository userRepository,
                        AddressRepository addressRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
    }

    private OrderResponse mapToDTO(Order order) {

        Address address = order.getAddress();

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .username(order.getUser().getUsername())
                // Giả định bảng Order liên kết với bảng Shop. Nếu chỉ lưu số thì dùng order.getShopId()
                .shopId(order.getShop() != null ? order.getShop().getId() : null)
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .createdAt(order.getCreatedAt())
                // Lấy thông tin từ object Address (dựa theo cấu trúc Address)
                .shippingName(address.getFullName())
                .shippingPhone(address.getPhone())
                .addressLine(address.getAddressLine())
                .city(address.getCity())
                .district(address.getDistrict())
                .ward(address.getWard())
                .build();
    }

    private Order getOrderOrThrow(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    public List<OrderResponse> getAllOrders() {

        return repository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public OrderResponse getOrderById(Integer id) {

        Order order = getOrderOrThrow(id);

        return mapToDTO(order);
    }

    public List<OrderResponse> getOrdersByUser(Integer userId) {

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        return repository.findByUserId(userId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Address does not belong to this user");
        }

        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setTotalPrice(request.getTotalPrice());
        order.setStatus(OrderStatus.PENDING);

        Order saved = repository.save(order);

        return mapToDTO(saved);
    }

    @Transactional
    public OrderResponse updateStatus(Integer id, OrderStatus status) {

        Order order = getOrderOrThrow(id);

        order.setStatus(status);

        return mapToDTO(repository.save(order));
    }

    @Transactional
    public void deleteOrder(Integer id) {

        Order order = getOrderOrThrow(id);

        repository.delete(order);
    }
}