package com.ecommerce.backend.service.admin;

import com.ecommerce.backend.dto.responses.seller.PageResponse;
import com.ecommerce.backend.dto.responses.admin.user.AdminUserDetailResponse;
import com.ecommerce.backend.dto.responses.admin.user.AdminUserResponse;
import com.ecommerce.backend.dto.responses.admin.user.AdminUserShopInfoResponse;
import com.ecommerce.backend.entity.Shop;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.enums.Role;
import com.ecommerce.backend.enums.UserStatus;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.ShopRepository;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    public AdminUserService(UserRepository userRepository, ShopRepository shopRepository) {
        this.userRepository = userRepository;
        this.shopRepository = shopRepository;
    }

    // LIST
    public PageResponse<AdminUserResponse> getUsers(
            int page,
            int size,
            Role role,
            UserStatus status,
            String keyword) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<User> users = userRepository.searchUsers(role, status, keyword, pageable);

        return new PageResponse<>(
                users.getContent().stream().map(this::mapToDTO).toList(),
                users.getNumber(),
                users.getSize(),
                users.getTotalElements(),
                users.getTotalPages());
    }

    // GET BY ID
    public AdminUserDetailResponse getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return mapToDetailDTO(user);
    }

    // CHANGE STATUS (ACTIVE -> BLOCKED or vice versa)
    public void changeStatus(Integer id, UserStatus newStatus) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setStatus(newStatus);
        userRepository.save(user);
    }

    // UPDATE ROLE
    public void updateRole(Integer id, Role newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == newRole) {
            return;
        }

        if (user.getRole() == Role.CUSTOMER && newRole == Role.SELLER) {
            user.setRole(newRole);
            userRepository.save(user);
        } else {
            throw new com.ecommerce.backend.exception.BadRequestException(
                    "Invalid role transition. Only upgrading from CUSTOMER to SELLER is allowed.");
        }
    }

    // MAPPER
    private AdminUserResponse mapToDTO(User user) {
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

    private AdminUserDetailResponse mapToDetailDTO(User user) {
        AdminUserDetailResponse response = AdminUserDetailResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .avatar(user.getAvatar())
                .provider(user.getProvider())
                .createdAt(user.getCreatedAt())
                .build();

        if (user.getRole() == Role.SELLER) {
            shopRepository.findByUserId(user.getId()).ifPresent(shop -> {
                response.setShop(mapToShopInfo(shop));
            });
        }

        return response;
    }

    private AdminUserShopInfoResponse mapToShopInfo(Shop shop) {
        return AdminUserShopInfoResponse.builder()
                .id(shop.getId())
                .shopName(shop.getShopName())
                .status(shop.getStatus())
                .description(shop.getDescription())
                .ratingAvg(shop.getRatingAvg() != null ? shop.getRatingAvg() : java.math.BigDecimal.ZERO)
                .totalOrders(shop.getTotalOrders() != null ? shop.getTotalOrders() : 0)
                .totalRevenue(shop.getTotalRevenue() != null ? shop.getTotalRevenue() : java.math.BigDecimal.ZERO)
                .build();
    }

    public List<String> autocompleteUsers(String keyword) {
        String k = (keyword == null) ? "" : keyword.trim();

        if (k.isEmpty()) {
            return List.of();
        }

        return userRepository.autocompleteUsers(k)
                .stream()
                .limit(5)
                .toList();
    }

}
