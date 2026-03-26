package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.UserRequest;
import com.ecommerce.backend.dto.responses.UserResponse;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.enums.UserStatus;
import com.ecommerce.backend.exception.BadRequestException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserResponse login(UserRequest request) {

        // 1. validate input
        if (request.getPassword() == null ||
                (request.getUsername() == null && request.getEmail() == null)) {
            throw new BadRequestException("Thiếu username/email hoặc password");
        }

        // 2. tìm user
        User user;

        if (request.getUsername() != null) {
            user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));
        } else {
            user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));
        }

        // 3. check status
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new BadRequestException("Tài khoản đã bị khóa");
        }

        // 4. check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu không đúng");
        }

        // 5. return
        return mapToResponse(user);
    }

    private UserResponse mapToResponse(User user) {
        UserResponse res = new UserResponse();

        res.setId(user.getId());
        res.setFullName(user.getFullName());
        res.setUsername(user.getUsername());
        res.setEmail(user.getEmail());
        res.setPhone(user.getPhone());
        res.setRole(user.getRole());
        res.setStatus(user.getStatus());
        res.setCreatedAt(user.getCreatedAt());

        return res;
    }
}