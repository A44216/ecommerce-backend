package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.LoginRequest;
import com.ecommerce.backend.dto.requests.ResetPasswordRequest;
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

        String username = request.getUsername() != null ? request.getUsername().trim().toLowerCase() : null;
        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null;
        String password = request.getPassword();

        // 1. Validate
        if (password == null || password.isEmpty()
                || ((username == null || username.isEmpty())
                && (email == null || email.isEmpty()))) {
            throw new BadRequestException("INVALID_INPUT");
        }

        // 2. tìm user
        User user;

        if (username != null) {
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
        if (fullName == null || fullName.isEmpty()) {
            throw new BadRequestException("INVALID_FULLNAME");
        }

        String phone = request.getPhone();
        if (phone != null) {
            phone = phone.trim();
            if (phone.isEmpty()) {
                phone = null;
            }
        }

        if (username == null || !username.matches("^[a-zA-Z0-9_]{7,}$")) {
            throw new BadRequestException("INVALID_USERNAME");
        }
        if (email == null || email.isEmpty()) {
            throw new BadRequestException("INVALID_EMAIL");
        }
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new BadRequestException("INVALID_EMAIL");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new BadRequestException("INVALID_PASSWORD");
        }
        if (phone != null && !phone.matches("^0\\d{9}$")) {
            throw new BadRequestException("INVALID_PHONE");
        }

        username = username.toLowerCase();
        email = email.toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("USERNAME_EXIST");
        }

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("EMAIL_EXIST");
        }

        if (phone != null && userRepository.existsByPhone(phone)) {
            throw new BadRequestException("PHONE_EXIST");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPhone(phone);

        String rawPassword = request.getPassword();
        if (rawPassword != null) {
            rawPassword = rawPassword.trim();
        }
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(request.getRole());
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        return mapToResponse(user);
    }

    public void resetPassword(ResetPasswordRequest request) {

        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null;

        String newPassword = request.getNewPassword();

        // 1. Validate input
        if (email == null || email.isEmpty()) {
            throw new BadRequestException("INVALID_EMAIL");
        }

        if (newPassword == null || newPassword.trim().length() < 6) {
            throw new BadRequestException("INVALID_PASSWORD");
        }

        // 2. tìm user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND"));

        // 3. update password
        user.setPassword(passwordEncoder.encode(newPassword.trim()));

        userRepository.save(user);
    }

}