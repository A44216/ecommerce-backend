package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.LoginRequest;
import com.ecommerce.backend.dto.requests.UserRequest;
import com.ecommerce.backend.dto.responses.LoginResponse;
import com.ecommerce.backend.dto.responses.UserResponse;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.enums.UserStatus;
import com.ecommerce.backend.exception.BadRequestException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    public LoginResponse login(LoginRequest request) {

        String username = request.getUsername() != null ? request.getUsername().trim() : null;
        String email = request.getEmail() != null ? request.getEmail().trim() : null;
        String password = request.getPassword() != null ? request.getPassword().trim() : null;

        // 1. Validate
        if (password == null || password.isEmpty()
                || ((username == null || username.isEmpty())
                && (email == null || email.isEmpty()))) {
            throw new BadRequestException("INVALID_INPUT");
        }

        // 2. tìm user
        User user;

        if (username != null && !username.isEmpty()) {
            user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND"));
        } else {
            user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND"));
        }

        // 3. check status
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new BadRequestException("ACCOUNT_BLOCKED");
        }

        // 4. check password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("WRONG_PASSWORD");
        }

        // 5. generate JWT
        String token = jwtService.generateToken(user.getId() + ":" + user.getRole().name());
        // 6. response
        LoginResponse res = new LoginResponse();
        res.setId(user.getId());
        res.setUsername(user.getUsername());
        res.setEmail(user.getEmail());
        res.setRole(user.getRole().name());
        res.setToken(token);

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

    public UserResponse register(UserRequest request) {
        String username = request.getUsername() != null ? request.getUsername().trim() : null;
        String email = request.getEmail() != null ? request.getEmail().trim() : null;
        String fullName = request.getFullName() != null ? request.getFullName().trim() : null;
        String phone = request.getPhone() != null ? request.getPhone().trim() : null;

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("USERNAME_EXIST");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("EMAIL_EXIST");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPhone(phone);

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // QUAN TRỌNG
        user.setRole(request.getRole());
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        return mapToResponse(user);
    }

}