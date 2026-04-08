package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.OrderRequest;
import com.ecommerce.backend.dto.responses.OrderItemResponse; // <-- THÊM IMPORT NÀY
import com.ecommerce.backend.dto.responses.OrderResponse;
import com.ecommerce.backend.entity.Address;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.enums.OrderStatus;
import com.ecommerce.backend.exception.BadRequestException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.AddressRepository;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.ShopRepository;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ShopRepository shopRepository;
    private final com.ecommerce.backend.repository.ProductRepository productRepository;
    private final com.ecommerce.backend.repository.OrderItemRepository orderItemRepository;

    public OrderService(OrderRepository repository,
                        UserRepository userRepository,
                        AddressRepository addressRepository,
                        ShopRepository shopRepository,
                        com.ecommerce.backend.repository.ProductRepository productRepository,
                        com.ecommerce.backend.repository.OrderItemRepository orderItemRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.shopRepository = shopRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
    }

    private OrderResponse mapToDTO(Order order) {

        Address address = order.getAddress();

        //CHUYỂN ĐỔI DANH SÁCH SẢN PHẨM ---
        List<OrderItemResponse> itemResponses = new ArrayList<>();

        // Kiểm tra xem đơn hàng có danh sách sản phẩm không
        if (order.getOrderItems() != null) {
            itemResponses = order.getOrderItems().stream()
                    .map(item -> {
                        // Lấy URL của ảnh đầu tiên (nếu có)
                        String firstImageUrl = null;
                        if (item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty()) {
                            firstImageUrl = item.getProduct().getImages().get(0).getImageUrl();
                        }

                        return OrderItemResponse.builder()
                                .id(item.getId())
                                .productId(item.getProduct().getId())
                                .productName(item.getProduct().getName())
                                .productImage(firstImageUrl) // Truyền URL ảnh vừa lấy được
                                .price(item.getPrice())
                                .quantity(item.getQuantity())
                                .build();
                    })
                    .collect(Collectors.toList());
        }
        // ---------------------------------------------------

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .username(order.getUser().getUsername())
                .shopId(order.getShop() != null ? order.getShop().getId() : null)
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .createdAt(order.getCreatedAt())
                .shippingName(address.getFullName())
                .shippingPhone(address.getPhone())
                .addressLine(address.getAddressLine())
                .city(address.getCity())
                .district(address.getDistrict())
                .ward(address.getWard())

                // --- NHÉT DANH SÁCH SẢN PHẨM VÀO ĐÂY ---
                .orderItems(itemResponses)

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

        com.ecommerce.backend.entity.Shop shop = shopRepository.findById(request.getShopId())
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));

        // 1. Tạo vỏ Đơn hàng
        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setShop(shop);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setTotalPrice(request.getTotalPrice());
        order.setStatus(OrderStatus.PENDING);

        Order savedOrder = repository.save(order); // Lưu để lấy Order ID

        // 2. Lưu danh sách sản phẩm (Snapshot Data)
        if (request.getOrderItems() != null && !request.getOrderItems().isEmpty()) {
            List<com.ecommerce.backend.entity.OrderItem> orderItemsList = new java.util.ArrayList<>();

            for (OrderRequest.OrderItemRequest itemReq : request.getOrderItems()) {

                if (itemReq.getProductId() == null) {
                    throw new BadRequestException("Lỗi: Có một sản phẩm trong giỏ hàng bị thiếu ID!");
                }

                com.ecommerce.backend.entity.Product product = productRepository.findById(itemReq.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

                com.ecommerce.backend.entity.OrderItem orderItem = new com.ecommerce.backend.entity.OrderItem();
                orderItem.setOrder(savedOrder);
                orderItem.setProduct(product);
                orderItem.setPrice(itemReq.getPrice());
                orderItem.setQuantity(itemReq.getQuantity());

                // Lấy Tên và Ảnh tại thời điểm mua (Snapshot)
                orderItem.setProductName(product.getName());

                String firstImageUrl = null;
                if (product.getImages() != null && !product.getImages().isEmpty()) {
                    firstImageUrl = product.getImages().get(0).getImageUrl();
                }
                orderItem.setProductImage(firstImageUrl);

                orderItemsList.add(orderItem);
            }

            // Lưu tất cả OrderItems vào DB
            orderItemRepository.saveAll(orderItemsList);
            savedOrder.setOrderItems(orderItemsList);
        }

        return mapToDTO(savedOrder);
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