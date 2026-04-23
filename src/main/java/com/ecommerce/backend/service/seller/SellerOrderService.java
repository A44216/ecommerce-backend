package com.ecommerce.backend.service.seller;

import com.ecommerce.backend.dto.responses.seller.PageResponse;
import com.ecommerce.backend.dto.responses.seller.order.SellerOrderDetailResponse;
import com.ecommerce.backend.dto.responses.seller.order.SellerOrderItemResponse;
import com.ecommerce.backend.dto.responses.seller.order.SellerOrderResponse;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderItem;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.Shop;
import com.ecommerce.backend.enums.OrderStatus;
import com.ecommerce.backend.enums.PaymentMethod;
import com.ecommerce.backend.enums.PaymentStatus;
import com.ecommerce.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SellerOrderService {

    private final OrderRepository orderRepository;
    private final SellerShopService sellerShopService;

    public PageResponse<SellerOrderResponse> getOrders(
            OrderStatus status,
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus,
            String keyword,
            int page,
            int size
    ) {
        Integer shopId = sellerShopService.getMyShop().getId();

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        if (keyword != null) {
            keyword = keyword.trim();
        }

        Page<Order> orders = orderRepository.getOrders(
                shopId,
                status,
                paymentMethod,
                paymentStatus,
                keyword,
                pageable
        );

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

        String couponCode = order.getCoupon() != null ? order.getCoupon().getCode() : null;

        return SellerOrderDetailResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .sellerRevenue(Optional.ofNullable(order.getSubtotal()).orElse(BigDecimal.ZERO).subtract(Optional.ofNullable(order.getPlatformFeeAmount()).orElse(BigDecimal.ZERO)))
                .subtotal(order.getSubtotal())
                .platformFeeRate(order.getPlatformFeeRate())
                .platformFeeAmount(order.getPlatformFeeAmount())
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
    @Transactional
    public void updateOrderStatus(Integer orderId, OrderStatus status) {

        Integer shopId = sellerShopService.getMyShop().getId();

        Order order = orderRepository.findByIdAndShopId(orderId, shopId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderStatus oldStatus = order.getStatus();

        // Chặn CONFIRMED nếu chưa PAID (QR)
        if (status == OrderStatus.CONFIRMED
                && order.getPaymentMethod() == PaymentMethod.QR
                && order.getPaymentStatus() != PaymentStatus.PAID) {

            throw new RuntimeException("Cannot confirm unpaid order");
        }

        order.setStatus(status);

        // chỉ set completedAt lần đầu khi chuyển sang COMPLETED
        if (status == OrderStatus.COMPLETED && oldStatus != OrderStatus.COMPLETED) {
            order.setCompletedAt(LocalDateTime.now());

            if (order.getPaymentMethod() == PaymentMethod.COD) {
                order.setPaymentStatus(PaymentStatus.PAID);
            }

            if (order.getPaymentStatus() == PaymentStatus.PAID) {

                Shop shop = order.getShop();
                if (shop != null) {
                    shop.setTotalOrders(shop.getTotalOrders() + 1);

                    BigDecimal sellerRevenue = Optional.ofNullable(order.getSubtotal()).orElse(BigDecimal.ZERO)
                            .subtract(Optional.ofNullable(order.getPlatformFeeAmount()).orElse(BigDecimal.ZERO));
                    shop.setTotalRevenue(shop.getTotalRevenue().add(sellerRevenue));
                }

                if (order.getItems() != null) {
                    for (OrderItem item : order.getItems()) {
                        Product product = item.getProduct();
                        if (product != null) {
                            product.setSoldCount(product.getSoldCount() + item.getQuantity());
                        }
                    }
                }
            }
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
                .orderCode(order.getOrderCode())
                .status(order.getStatus())
                .customerName(order.getUser().getFullName())
                .phone(order.getShippingPhone())
                .sellerRevenue(Optional.ofNullable(order.getSubtotal()).orElse(BigDecimal.ZERO).subtract(Optional.ofNullable(order.getPlatformFeeAmount()).orElse(BigDecimal.ZERO)))
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
                .unitPrice(item.getPrice())
                .quantity(item.getQuantity())
                .totalPrice(item.getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .build();
    }

    public List<String> autocompleteOrders(String keyword) {
        String k = (keyword == null) ? "" : keyword.trim();

        if (k.isEmpty()) {
            return List.of();
        }

        return orderRepository.autocompleteOrders(k)
                .stream()
                .distinct()
                .limit(5)
                .toList();
    }

}