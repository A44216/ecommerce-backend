package com.ecommerce.backend.service.seller;

import com.ecommerce.backend.dto.requests.seller.shop.SellerShopRequest;
import com.ecommerce.backend.dto.responses.seller.shop.SellerShopResponse;
import com.ecommerce.backend.entity.Shop;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.exception.BadRequestException;
import com.ecommerce.backend.repository.ShopRepository;
import com.ecommerce.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SellerShopService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;

    // GET CURRENT USER (KHÔNG CACHE - FIX THREAD SAFETY)
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // GET CURRENT USER ID (OPTIMIZED)
    private Integer getCurrentUserId() {
        return getCurrentUser().getId();
    }

    // GET MY SHOP
    public SellerShopResponse getMyShop() {

        Shop shop = shopRepository.findByUserIdFetchUser(getCurrentUserId())
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        return mapToDTO(shop);
    }

    // CREATE SHOP
    public SellerShopResponse createShop(SellerShopRequest request) {

        Integer userId = getCurrentUserId();

        if (shopRepository.findByUserId(userId).isPresent()) {
            throw new BadRequestException("User already has a shop");
        }

        User user = getCurrentUser();

        Shop shop = new Shop();
        mapRequest(shop, request, user);

        return mapToDTO(shopRepository.save(shop));
    }

    // UPDATE SHOP
    public SellerShopResponse updateShop(SellerShopRequest request) {

        Integer userId = getCurrentUserId();

        Shop shop = shopRepository.findByUserIdFetchUser(userId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        mapRequest(shop, request, shop.getUser());

        return mapToDTO(shopRepository.save(shop));
    }

    // UPDATE AVATAR
    public SellerShopResponse updateAvatar(String avatar) {

        Integer userId = getCurrentUserId();

        Shop shop = shopRepository.findByUserIdFetchUser(userId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

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
                .totalOrders(shop.getTotalOrders())
                .totalRevenue(shop.getTotalRevenue())
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