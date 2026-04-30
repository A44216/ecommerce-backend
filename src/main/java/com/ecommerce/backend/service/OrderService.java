package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.OrderRequest;
import com.ecommerce.backend.dto.responses.OrderItemResponse;
import com.ecommerce.backend.dto.responses.OrderResponse;
import com.ecommerce.backend.entity.Address;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.enums.NotificationType;
import com.ecommerce.backend.enums.OrderStatus;
import com.ecommerce.backend.exception.BadRequestException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.AddressRepository;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.ShopRepository;
import com.ecommerce.backend.repository.UserRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Getter
@Setter
public class OrderService {

    private final OrderRepository repository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ShopRepository shopRepository;
    private final com.ecommerce.backend.repository.ProductRepository productRepository;
    private final com.ecommerce.backend.repository.OrderItemRepository orderItemRepository;
    private final NotificationService notificationService;
    private final com.ecommerce.backend.repository.CouponRepository couponRepository;

    public OrderService(OrderRepository repository,
                        UserRepository userRepository,
                        AddressRepository addressRepository,
                        ShopRepository shopRepository,
                        com.ecommerce.backend.repository.ProductRepository productRepository,
                        com.ecommerce.backend.repository.OrderItemRepository orderItemRepository, 
                        NotificationService notificationService,
                        com.ecommerce.backend.repository.CouponRepository couponRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.shopRepository = shopRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
        this.notificationService = notificationService;
        this.couponRepository = couponRepository;
    }

    private OrderResponse mapToDTO(Order order) {

        Address address = order.getAddress();

        // ==========================================
        // XỬ LÝ GỘP CHUỖI ĐỊA CHỈ TẠI ĐÂY
        // ==========================================
        String fullAddress = address.getAddressLine() != null ? address.getAddressLine() : "";

        if (address.getWard() != null && !address.getWard().isEmpty()) {
            fullAddress += ", " + address.getWard();
        }
        if (address.getDistrict() != null && !address.getDistrict().isEmpty()) {
            fullAddress += ", " + address.getDistrict();
        }
        if (address.getCity() != null && !address.getCity().isEmpty()) {
            fullAddress += ", " + address.getCity();
        }

        //CHUYỂN ĐỔI DANH SÁCH SẢN PHẨM ---
        List<OrderItemResponse> itemResponses = new ArrayList<>();

        // Kiểm tra xem đơn hàng có danh sách sản phẩm không
        if (order.getItems() != null) {
            itemResponses = order.getItems().stream()
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
                .orderCode(order.getOrderCode())
                .userId(order.getUser().getId())
                .username(order.getUser().getUsername())
                .shopId(order.getShop() != null ? order.getShop().getId() : null)
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .platformFeeRate(order.getPlatformFeeRate())
                .platformFeeAmount(order.getPlatformFeeAmount())
                .couponId(order.getCoupon() != null ? order.getCoupon().getId() : null)
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .createdAt(order.getCreatedAt())
                
                // Sử dụng thông tin snapshot từ bảng orders nếu có, nếu không thì fallback về bảng addresses
                .shippingName(order.getShippingName() != null ? order.getShippingName() : address.getFullName())
                .shippingPhone(order.getShippingPhone() != null ? order.getShippingPhone() : address.getPhone())
                .addressLine(order.getShippingAddress() != null ? order.getShippingAddress() : fullAddress)

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
        
        // Phát sinh mã đơn hàng duy nhất
        String orderCode = "ORD-" + System.currentTimeMillis() + "-" + java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        order.setOrderCode(orderCode);
        
        order.setUser(user);
        order.setAddress(address);
        order.setShop(shop);
        order.setPaymentMethod(request.getPaymentMethod());

        // Tự động thiết lập trạng thái thanh toán
        if (request.getPaymentMethod() == com.ecommerce.backend.enums.PaymentMethod.QR) {
            order.setPaymentStatus(com.ecommerce.backend.enums.PaymentStatus.PAID);
        } else {
            order.setPaymentStatus(com.ecommerce.backend.enums.PaymentStatus.UNPAID);
        }

        // Snapshot thông tin vận chuyển tại thời điểm đặt hàng
        order.setShippingName(address.getFullName());
        order.setShippingPhone(address.getPhone());

        String fullAddress = address.getAddressLine() != null ? address.getAddressLine() : "";
        if (address.getWard() != null && !address.getWard().isEmpty()) {
            fullAddress += ", " + address.getWard();
        }
        if (address.getDistrict() != null && !address.getDistrict().isEmpty()) {
            fullAddress += ", " + address.getDistrict();
        }
        if (address.getCity() != null && !address.getCity().isEmpty()) {
            fullAddress += ", " + address.getCity();
        }
        order.setShippingAddress(fullAddress);
        order.setTotalPrice(request.getTotalPrice());
        order.setSubtotal(request.getSubtotal());
        order.setDiscountAmount(request.getDiscountAmount());
        order.setStatus(OrderStatus.PENDING);

        // Xử lý Platform Fee (Mặc định 5%)
        java.math.BigDecimal platformFeeRate = new java.math.BigDecimal("5.00");
        order.setPlatformFeeRate(platformFeeRate);
        if (request.getSubtotal() != null) {
            order.setPlatformFeeAmount(request.getSubtotal().multiply(platformFeeRate).divide(new java.math.BigDecimal("100")));
        } else {
            order.setPlatformFeeAmount(java.math.BigDecimal.ZERO);
        }

        // Xử lý Coupon
        if (request.getCouponId() != null) {
            com.ecommerce.backend.entity.Coupon coupon = couponRepository.findById(request.getCouponId())
                    .orElseThrow(() -> new BadRequestException("Mã giảm giá không hợp lệ"));
            order.setCoupon(coupon);
        }

        // Biến savedOrder đã được khai báo ở đây
        Order savedOrder = repository.save(order);

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
            savedOrder.setItems(orderItemsList);
        }

        // ==========================================
        // 3. TẠO THÔNG BÁO TỰ ĐỘNG
        // ==========================================
        notificationService.createNotification(
                savedOrder.getUser().getId(),
                "Đặt hàng thành công!",
                "Đơn hàng #" + savedOrder.getId() + " đã được tạo và đang chờ người bán xác nhận.",
                NotificationType.ORDER,
                savedOrder.getId()
        );

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

    @Transactional
    public OrderResponse cancelOrder(Integer orderId) {
        Order order = getOrderOrThrow(orderId);

        // Chỉ cho phép hủy khi đơn hàng đang ở trạng thái PENDING (Chờ xác nhận)
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Chỉ có thể hủy đơn hàng khi đang chờ xác nhận!");
        }

        // Cập nhật trạng thái
        order.setStatus(OrderStatus.CANCELED);
        Order savedOrder = repository.save(order);

        // Bắn thông báo về hệ thống
        notificationService.createNotification(
                savedOrder.getUser().getId(),
                "Đơn hàng đã bị hủy",
                "Đơn hàng #" + savedOrder.getId() + " của bạn đã được hủy thành công.",
                NotificationType.ORDER,
                savedOrder.getId()
        );

        return mapToDTO(savedOrder);
    }
}