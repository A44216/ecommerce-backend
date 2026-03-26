package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.LoginRequest;
import com.ecommerce.backend.dto.responses.LoginResponse;
import com.ecommerce.backend.dto.responses.UserResponse;
import com.ecommerce.backend.entity.User;
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

    public LoginResponse login(LoginRequest request) {

        User user;

        if (request.getUsername() != null) {
            user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));
        } else {
            user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Sai mật khẩu");
        }

        LoginResponse res = new LoginResponse();
        res.setId(user.getId());
        res.setUsername(user.getUsername());
        res.setEmail(user.getEmail());
        res.setRole(user.getRole().name());

        return res;
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