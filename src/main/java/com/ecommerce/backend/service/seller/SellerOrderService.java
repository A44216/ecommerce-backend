package com.ecommerce.backend.service.seller;

import com.ecommerce.backend.dto.responses.seller.PageResponse;
import com.ecommerce.backend.dto.responses.seller.order.SellerOrderDetailResponse;
import com.ecommerce.backend.dto.responses.seller.order.SellerOrderItemResponse;
import com.ecommerce.backend.dto.responses.seller.order.SellerOrderResponse;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderItem;
import com.ecommerce.backend.enums.OrderStatus;
import com.ecommerce.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerOrderService {

    private final OrderRepository orderRepository;
    private final SellerShopService sellerShopService;

    public PageResponse<SellerOrderResponse> getOrders(
            OrderStatus status,
            int page,
            int size
    ) {
        Integer shopId = sellerShopService.getMyShop().getId();

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<Order> orders = (status == null)
                ? orderRepository.findByShopId(shopId, pageable)
                : orderRepository.findByShopIdAndStatus(shopId, status, pageable);

        List<SellerOrderResponse> content = orders
                .map(this::mapToDTO)
                .getContent();

        return new PageResponse<>(
                content,
                orders.getNumber(),
                orders.getSize(),
                orders.getTotalElements(),
                orders.getTotalPages()
        );
    }

    // ORDER DETAIL
    public SellerOrderDetailResponse getOrderDetail(Integer orderId) {

        Integer shopId = sellerShopService.getMyShop().getId();

        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getShop().getId().equals(shopId)) {
            throw new RuntimeException("Not your shop order");
        }

        List<SellerOrderItemResponse> items = order.getItems()
                .stream()
                .map(this::mapItem)
                .toList();

        return SellerOrderDetailResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .totalPrice(order.getTotalPrice())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .commissionRate(order.getCommissionRate())
                .commissionAmount(order.getCommissionAmount())
                .createdAt(order.getCreatedAt())
                .completedAt(order.getCompletedAt())
                .shippingName(order.getShippingName())
                .shippingPhone(order.getShippingPhone())
                .shippingAddress(order.getShippingAddress())
                .customerName(order.getUser().getFullName())
                .items(items)
                .build();
    }

    // UPDATE STATUS
    public void updateOrderStatus(Integer orderId, OrderStatus status) {

        Integer shopId = sellerShopService.getMyShop().getId();

        Order order = orderRepository.findByIdAndShopId(orderId, shopId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderStatus oldStatus = order.getStatus();

        order.setStatus(status);

        // chỉ set completedAt lần đầu khi chuyển sang COMPLETED
        if (status == OrderStatus.COMPLETED && oldStatus != OrderStatus.COMPLETED) {
            order.setCompletedAt(LocalDateTime.now());
        }

        orderRepository.save(order);
    }

    // MAPPER
    private SellerOrderResponse mapToDTO(Order order) {

        String image = null;

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            image = order.getItems().getFirst().getProductImage();
        }

        return SellerOrderResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .customerName(order.getUser().getFullName())
                .totalPrice(order.getTotalPrice())
                .createdAt(order.getCreatedAt())
                .paymentStatus(order.getPaymentStatus())
                .imageOrder(image)
                .build();
    }

    private SellerOrderItemResponse mapItem(OrderItem item) {
        return SellerOrderItemResponse.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProductName())
                .productImage(item.getProductImage())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .build();
    }
}