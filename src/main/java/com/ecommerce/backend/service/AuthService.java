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

        // 1. validate input
        if (request.getPassword() == null ||
                (request.getUsername() == null && request.getEmail() == null)) {
            throw new BadRequestException("INVALID_INPUT");
        }

        // 2. tìm user
        User user;

        if (request.getUsername() != null) {
            user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND"));
        } else {
            user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND"));
        }

        // 3. check status
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new BadRequestException("ACCOUNT_BLOCKED");
        }

        // 4. check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("WRONG_PASSWORD");
        }

        // 5. generate JWT
        String token = jwtService.generateToken(user.getUsername());

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

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("USERNAME_EXIST");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("EMAIL_EXIST");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // QUAN TRỌNG
        user.setRole(request.getRole());
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        return mapToResponse(user);
    }

}