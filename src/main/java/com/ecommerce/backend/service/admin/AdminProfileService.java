package com.ecommerce.backend.service.admin;

import com.ecommerce.backend.dto.requests.admin.profile.AdminProfileInfoRequest;
import com.ecommerce.backend.dto.responses.admin.profile.AdminProfileInfoResponse;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.exception.BadRequestException;
import com.ecommerce.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminProfileService {

    private final UserRepository userRepository;

    public AdminProfileInfoResponse getProfile(Authentication authentication) {
        User user = getUserFromAuth(authentication);
        return mapToResponse(user);
    }

    @Transactional
    public AdminProfileInfoResponse updateProfile(
            Authentication authentication,
            AdminProfileInfoRequest request
    ) {
        User user = getUserFromAuth(authentication);

        user.setFullName(request.getFullName());

        // Xử lý Email: Ném BadRequestException nếu trống
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BadRequestException("Email không được để trống.");
        }
        user.setEmail(request.getEmail());

        // Xử lý chuỗi rỗng cho Phone
        if (request.getPhone() != null && request.getPhone().isBlank()) {
            user.setPhone(null);
        } else {
            user.setPhone(request.getPhone());
        }

        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        userRepository.save(user);

        return mapToResponse(user);
    }

    // Lấy user từ token
    private User getUserFromAuth(Authentication authentication) {
        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private AdminProfileInfoResponse mapToResponse(User user) {
        AdminProfileInfoResponse res = new AdminProfileInfoResponse();

        res.setFullName(user.getFullName());
        res.setEmail(user.getEmail());
        res.setPhone(user.getPhone());
        res.setAvatar(user.getAvatar());

        return res;
    }
}