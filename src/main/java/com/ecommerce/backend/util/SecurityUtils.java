package com.ecommerce.backend.util;

import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    // Lấy Username từ SecurityContext
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }

     // Lấy đối tượng User đầy đủ từ Database dựa trên Token
    public User getCurrentUser() {
        String username = getCurrentUsername();
        if (username == null) {
            throw new ResourceNotFoundException("USER_NOT_AUTHENTICATED");
        }
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND"));
    }

    // Lấy ID của User đang đăng nhập (Tiện cho việc lưu Foreign Key)
    public Integer getCurrentUserId() {
        return getCurrentUser().getId();
    }
}