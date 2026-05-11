package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.ShopRequest;
import com.ecommerce.backend.dto.responses.ShopResponse;
import com.ecommerce.backend.entity.Shop;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.enums.NotificationType;
import com.ecommerce.backend.enums.Role;
import com.ecommerce.backend.repository.ShopRepository;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ShopService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ShopService(ShopRepository shopRepository,
                       UserRepository userRepository,
                       NotificationService notificationService) {
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    // ENTITY -> RESPONSE
    private ShopResponse mapToDTO(Shop shop) {
        return ShopResponse.builder()
                .id(shop.getId())
                .shopName(shop.getShopName())
                .description(shop.getDescription())
                .ownerName(shop.getUser().getFullName()) // truyền đúng tên chủ shop
                .status(shop.getStatus()) // truyền nguyên object ShopStatus, không dùng .name()
                .createdAt(shop.getCreatedAt()) // bổ sung trường thứ 6 bị thiếu
                .avatar(shop.getAvatar())
                .ratingAvg(shop.getRatingAvg())
                .ratingCount(shop.getRatingCount())
                .build();
    }

    // REQUEST -> ENTITY
    private void mapRequestToShop(Shop shop, ShopRequest request, User user) {
        shop.setShopName(request.getShopName());
        shop.setDescription(request.getDescription());
        shop.setStatus(request.getStatus());
        shop.setUser(user);
    }

    // tất cả shop
    public List<ShopResponse> getAllShops() {

        List<Shop> shops = shopRepository.findAll();

        return shops.stream()
                .map(this::mapToDTO)
                .toList();
    }

    // shop theo id
    public ShopResponse getShopById(Integer id) {

        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        return mapToDTO(shop);
    }

    // tạo shop
    @org.springframework.transaction.annotation.Transactional
    public ShopResponse createShop(ShopRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // XÓA SẠCH bản ghi cũ (Nếu có) trước khi tạo mới
        System.out.println(">>> PUBLIC SERVICE: ĐANG XÓA SHOP CŨ CHO USER ID: " + request.getUserId());
        shopRepository.deleteByUserIdNative(request.getUserId());
        shopRepository.flush();

        Shop shop = new Shop();

        mapRequestToShop(shop, request, user);
        shop.setStatus(com.ecommerce.backend.enums.ShopStatus.PENDING); // Bắt buộc là PENDING khi mới tạo

        Shop saved = shopRepository.save(shop);

        // THÊM: Gửi thông báo cho Admin khi có shop mới đăng ký
        List<User> admins = userRepository.findByRole(Role.ADMIN);
        for (User admin : admins) {
            notificationService.createNotification(
                    admin.getId(),
                    "Có yêu cầu đăng ký mở Shop mới",
                    "Người dùng " + user.getFullName() + " vừa gửi yêu cầu mở Shop: " + shop.getShopName() + ".",
                    NotificationType.SHOP,
                    saved.getId()
            );
        }

        return mapToDTO(saved);
    }

    // cập nhật shop
    @org.springframework.transaction.annotation.Transactional
    public ShopResponse updateShop(Integer id, ShopRequest request) {

        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        mapRequestToShop(shop, request, shop.getUser());

        Shop updated = shopRepository.save(shop);

        return mapToDTO(updated);
    }

    // xóa
    @org.springframework.transaction.annotation.Transactional
    public void deleteShop(Integer id) {
        shopRepository.deleteById(id);
    }

    // shop theo user
    public ShopResponse getShopByUser(Integer userId) {

        Shop shop = shopRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        return mapToDTO(shop);
    }

}