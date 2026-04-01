package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.ShopRequest;
import com.ecommerce.backend.dto.responses.ShopResponse;
import com.ecommerce.backend.entity.Shop;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.repository.ShopRepository;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShopService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;

    public ShopService(ShopRepository shopRepository,
                       UserRepository userRepository) {
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
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
    public ShopResponse createShop(ShopRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Shop shop = new Shop();

        mapRequestToShop(shop, request, user);

        Shop saved = shopRepository.save(shop);

        return mapToDTO(saved);
    }

    // cập nhật shop
    public ShopResponse updateShop(Integer id, ShopRequest request) {

        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        mapRequestToShop(shop, request, shop.getUser());

        Shop updated = shopRepository.save(shop);

        return mapToDTO(updated);
    }

    // xóa
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