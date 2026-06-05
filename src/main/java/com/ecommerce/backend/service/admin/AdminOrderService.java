package com.ecommerce.backend.service.admin;

import com.ecommerce.backend.dto.responses.admin.order.AdminOrderDetailResponse;
import com.ecommerce.backend.dto.responses.admin.order.AdminOrderItemResponse;
import com.ecommerce.backend.dto.responses.admin.order.AdminOrderResponse;
import com.ecommerce.backend.dto.responses.seller.PageResponse;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderItem;
import com.ecommerce.backend.enums.OrderStatus;
import com.ecommerce.backend.enums.PaymentMethod;
import com.ecommerce.backend.enums.PaymentStatus;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOrderService {

    private final OrderRepository orderRepository;

    public PageResponse<AdminOrderResponse> getOrders(
            int page,
            int size,
            String keyword,
            Integer shopId,
            OrderStatus status,
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus,
            String sortBy,
            String direction) {

        Sort sort = (direction != null && direction.equalsIgnoreCase("asc"))
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Order> orders = orderRepository.adminSearchOrders(
                shopId,
                keyword,
                status,
                paymentMethod,
                paymentStatus,
                pageable);

        return new PageResponse<>(
                orders.getContent().stream().map(this::mapToDTO).toList(),
                orders.getNumber(),
                orders.getSize(),
                orders.getTotalElements(),
                orders.getTotalPages());
    }

    public AdminOrderDetailResponse getOrderById(Integer id) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return mapToDetailDTO(order);
    }

    private AdminOrderResponse mapToDTO(Order order) {

        String imageOrder = null;

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            imageOrder = order.getItems()
                    .getFirst()
                    .getProductImage();
        }

        return AdminOrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .username(order.getUser() != null ? order.getUser().getUsername() : null)
                .fullName(order.getUser() != null ? order.getUser().getFullName() : null)
                .shopId(order.getShop() != null ? order.getShop().getId() : null)
                .shopName(order.getShop() != null ? order.getShop().getShopName() : null)
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .totalPrice(order.getTotalPrice())
                .platformFeeAmount(order.getPlatformFeeAmount())
                .createdAt(order.getCreatedAt())
                .imageOrder(imageOrder)
                .build();
    }

    private AdminOrderDetailResponse mapToDetailDTO(Order order) {

        BigDecimal subtotal = order.getSubtotal() != null ? order.getSubtotal() : BigDecimal.ZERO;
        BigDecimal platformFee = order.getPlatformFeeAmount() != null ? order.getPlatformFeeAmount() : BigDecimal.ZERO;
        BigDecimal totalPrice = order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO;
        BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;

        // Tính phí ship: totalPrice = subtotal + shippingFee - discount → shippingFee =
        // totalPrice - subtotal + discount
        BigDecimal shippingFee = totalPrice.subtract(subtotal).add(discount);
        if (shippingFee.compareTo(BigDecimal.ZERO) < 0) {
            shippingFee = BigDecimal.ZERO;
        }

        // Seller nhận = subtotal + phí ship - phí nền tảng
        BigDecimal sellerReceived = subtotal.add(shippingFee).subtract(platformFee);

        return AdminOrderDetailResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .fullName(order.getUser() != null ? order.getUser().getFullName() : null)
                .shopId(order.getShop() != null ? order.getShop().getId() : null)
                .shopName(order.getShop() != null ? order.getShop().getShopName() : null)
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .platformFeeRate(order.getPlatformFeeRate())
                .platformFeeAmount(order.getPlatformFeeAmount())
                .totalPrice(order.getTotalPrice())
                .shippingFee(shippingFee)
                .sellerReceived(sellerReceived)
                .shippingName(order.getShippingName())
                .shippingPhone(order.getShippingPhone())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .completedAt(order.getCompletedAt())
                .items(order.getItems() != null ? order.getItems().stream().map(this::mapItemToDTO).toList() : null)
                .build();
    }

    private AdminOrderItemResponse mapItemToDTO(OrderItem item) {
        return AdminOrderItemResponse.builder()
                .id(item.getId())
                .productCode(item.getProduct().getProductCode())
                .productId(item.getProduct().getId())
                .productName(item.getProductName())
                .productImage(item.getProductImage())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .build();
    }

    public List<String> autocomplete(String keyword, Integer shopId) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return orderRepository.autocompleteAdminOrders(keyword.trim(), shopId);
    }

}
