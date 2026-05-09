package com.ecommerce.backend.service.admin;

import com.ecommerce.backend.dto.responses.admin.shop.AdminShopDetailResponse;
import com.ecommerce.backend.dto.responses.admin.shop.AdminShopResponse;
import com.ecommerce.backend.dto.responses.admin.shop.AdminShopAutocompleteResponse;
import com.ecommerce.backend.dto.responses.admin.user.AdminUserResponse;
import com.ecommerce.backend.dto.responses.seller.PageResponse;
import com.ecommerce.backend.entity.Shop;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.enums.ShopStatus;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.ShopRepository;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.service.NotificationService;
import com.ecommerce.backend.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminShopService {

    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public PageResponse<AdminShopResponse> getShops(int page, int size, ShopStatus status, String keyword,
            String sortBy, String sortDir) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Shop> shops = shopRepository.searchShops(status, keyword, pageable);

        return new PageResponse<>(
                shops.getContent().stream().map(this::mapToDTO).toList(),
                shops.getNumber(),
                shops.getSize(),
                shops.getTotalElements(),
                shops.getTotalPages());
    }

    public AdminShopDetailResponse getShopById(Integer id) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
        Integer totalProducts = productRepository.countByShopIdAndIsDeletedFalse(id);
        return mapToDetailDTO(shop, totalProducts);
    }

    @Transactional
    public void updateShopStatus(Integer id, ShopStatus status) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
        shop.setStatus(status);

        if (status == ShopStatus.APPROVED) {
            User user = shop.getUser();
            if (user != null && user.getRole() == com.ecommerce.backend.enums.Role.CUSTOMER) {
                user.setRole(com.ecommerce.backend.enums.Role.SELLER);
                userRepository.save(user);
            }
        }
        
        Shop savedShop = shopRepository.save(shop);

        // Thông báo cho User
        String title = "Cập nhật trạng thái Shop";
        String message = "";
        if (status == ShopStatus.APPROVED) {
            title = "Chúc mừng! Shop của bạn đã được duyệt";
            message = "Yêu cầu mở Shop '" + savedShop.getShopName() + "' của bạn đã được duyệt. Bạn có thể bắt đầu đăng bán sản phẩm ngay bây giờ.";
        } else if (status == ShopStatus.REJECTED) {
            title = "Yêu cầu mở Shop bị từ chối";
            message = "Rất tiếc, yêu cầu mở Shop '" + savedShop.getShopName() + "' của bạn đã bị từ chối.";
        } else if (status == ShopStatus.BLOCKED) {
            title = "Shop của bạn đã bị khóa";
            message = "Shop '" + savedShop.getShopName() + "' của bạn đã bị Admin khóa. Vui lòng liên hệ hỗ trợ để biết thêm chi tiết.";
        }

        if (!message.isEmpty()) {
            notificationService.createNotification(
                    savedShop.getUser().getId(),
                    title,
                    message,
                    NotificationType.SHOP,
                    savedShop.getId()
            );
        }
    }

    private AdminShopResponse mapToDTO(Shop shop) {
        return AdminShopResponse.builder()
                .id(shop.getId())
                .shopName(shop.getShopName())
                .email(shop.getEmail())
                .phone(shop.getPhone())
                .status(shop.getStatus())
                .avatar(shop.getAvatar())
                .ratingAvg(shop.getRatingAvg() != null ? shop.getRatingAvg() : BigDecimal.ZERO)
                .totalOrders(shop.getTotalOrders() != null ? shop.getTotalOrders() : 0)
                .totalRevenue(shop.getTotalRevenue() != null ? shop.getTotalRevenue() : BigDecimal.ZERO)
                .createdAt(shop.getCreatedAt())
                .build();
    }

    private AdminShopDetailResponse mapToDetailDTO(Shop shop, Integer totalProducts) {
        return AdminShopDetailResponse.builder()
                .id(shop.getId())
                .shopName(shop.getShopName())
                .description(shop.getDescription())
                .email(shop.getEmail())
                .phone(shop.getPhone())
                .address(shop.getAddress())
                .status(shop.getStatus())
                .avatar(shop.getAvatar())
                .ratingAvg(shop.getRatingAvg() != null ? shop.getRatingAvg() : BigDecimal.ZERO)
                .ratingCount(shop.getRatingCount() != null ? shop.getRatingCount() : 0)
                .totalProducts(totalProducts != null ? totalProducts : 0)
                .totalOrders(shop.getTotalOrders() != null ? shop.getTotalOrders() : 0)
                .totalRevenue(shop.getTotalRevenue() != null ? shop.getTotalRevenue() : BigDecimal.ZERO)
                .createdAt(shop.getCreatedAt())
                .owner(mapUserToDTO(shop.getUser()))
                .build();
    }

    private AdminUserResponse mapUserToDTO(User user) {
        if (user == null)
            return null;
        return AdminUserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .avatar(user.getAvatar())
                .build();
    }

    public List<AdminShopAutocompleteResponse> autocompleteShops(String keyword) {
        String k = (keyword == null) ? "" : keyword.trim();

        if (k.isEmpty()) {
            return List.of();
        }

        return shopRepository.autocompleteShops(k)
                .stream()
                .map(row -> new AdminShopAutocompleteResponse(
                        ((Number) row[0]).intValue(), // id
                        (String) row[1]              // label
                ))
                .distinct()
                .limit(5)
                .toList();
    }

}
