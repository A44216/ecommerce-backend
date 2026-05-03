package com.ecommerce.backend.service.seller;

import com.ecommerce.backend.dto.requests.seller.shop.SellerShopRequest;
import com.ecommerce.backend.dto.responses.seller.shop.SellerShopResponse;
import com.ecommerce.backend.entity.Shop;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.enums.NotificationType;
import com.ecommerce.backend.enums.Role;
import com.ecommerce.backend.enums.ShopStatus;
import com.ecommerce.backend.exception.BadRequestException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.ShopRepository;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.service.NotificationService;
import com.ecommerce.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SellerShopService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;

    // GET MY SHOP
    public SellerShopResponse getMyShop() {
        return mapToDTO(getMyShopEntity());
    }

    public Shop getMyShopEntity() {
        // Dùng trực tiếp ID từ Token
        return shopRepository.findByUserIdFetchUser(securityUtils.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
    }

    // CREATE SHOP
    @Transactional
    public SellerShopResponse createShop(SellerShopRequest request) {

        // Lấy User và ID từ SecurityUtils
        User user = securityUtils.getCurrentUser();
        Integer userId = user.getId();
        
        System.out.println(">>> ĐANG THỰC HIỆN XÓA SHOP CŨ CHO USER ID: " + userId);
        // XÓA SẠCH bản ghi cũ (Dùng Repository Modifying Query)
        shopRepository.deleteByUserIdNative(userId);
        shopRepository.flush();
        
        Shop shop = new Shop();
        mapRequest(shop, request, user);
        shop.setStatus(ShopStatus.PENDING);

        Shop savedShop = shopRepository.save(shop);

        // Thông báo cho Admin
        List<User> admins = userRepository.findByRole(Role.ADMIN);
        for (User admin : admins) {
            notificationService.createNotification(
                    admin.getId(),
                    "Yêu cầu mở Shop mới",
                    "Người dùng " + user.getFullName() + " vừa gửi yêu cầu đăng ký mở Shop: " + savedShop.getShopName() + ".",
                    NotificationType.SHOP,
                    savedShop.getId()
            );
        }

        return mapToDTO(savedShop);
    }

    // UPDATE SHOP
    @Transactional
    public SellerShopResponse updateShop(SellerShopRequest request) {

        Shop shop = shopRepository.findByUserIdFetchUser(securityUtils.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));

        mapRequest(shop, request, shop.getUser());

        return mapToDTO(shopRepository.save(shop));
    }

    // CANCEL SHOP REGISTRATION
    @Transactional
    public void cancelShopRegistration() {
        User user = securityUtils.getCurrentUser();
        Integer userId = user.getId();
        
        Shop shop = shopRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu đăng ký Shop"));

        if (shop.getStatus() != ShopStatus.PENDING) {
            throw new BadRequestException("Chỉ có thể hủy yêu cầu khi đang ở trạng thái chờ duyệt!");
        }

        // XÓA HOÀN TOÀN bằng SQL thuần qua Repository
        shopRepository.deleteByUserIdNative(userId);
        shopRepository.flush();
        System.out.println(">>> ĐÃ HỦY VÀ XÓA SHOP CỦA USER: " + userId);

        // Thông báo cho Admin
        List<User> admins = userRepository.findByRole(Role.ADMIN);
        for (User admin : admins) {
            notificationService.createNotification(
                    admin.getId(),
                    "Yêu cầu mở Shop đã bị hủy",
                    "Người dùng " + user.getFullName() + " đã hủy yêu cầu đăng ký mở Shop: " + shop.getShopName() + ".",
                    NotificationType.SHOP,
                    shop.getId() // Truyền ID để Admin có thể bấm vào xem
            );
        }
    }

    // UPDATE AVATAR
    public SellerShopResponse updateAvatar(String avatar) {

        Shop shop = shopRepository.findByUserIdFetchUser(securityUtils.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));

        shop.setAvatar(avatar);

        return mapToDTO(shopRepository.save(shop));
    }

    // MAP ENTITY -> DTO
    private SellerShopResponse mapToDTO(Shop shop) {
        return SellerShopResponse.builder()
                .id(shop.getId())
                .shopName(shop.getShopName())
                .description(shop.getDescription())
                .ownerName(shop.getUser().getFullName())
                .status(shop.getStatus())
                .createdAt(shop.getCreatedAt())
                .avatar(shop.getAvatar())
                .address(shop.getAddress())
                .ratingAvg(shop.getRatingAvg())
                .ratingCount(shop.getRatingCount())
                .phone(shop.getPhone())
                .email(shop.getEmail())
                .build();
    }

    // MAP REQUEST -> ENTITY
    private void mapRequest(Shop shop, SellerShopRequest request, User user) {

        shop.setUser(user);
        shop.setShopName(request.getShopName());
        shop.setDescription(request.getDescription());
        shop.setAddress(request.getAddress());
        shop.setPhone(request.getPhone());
        shop.setEmail(request.getEmail());
        shop.setAvatar(request.getAvatar());
    }
}