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


import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.ecommerce.backend.dto.requests.GoogleLoginRequest;
import com.ecommerce.backend.enums.Provider;
import com.ecommerce.backend.enums.Role;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    @org.springframework.beans.factory.annotation.Value("${google.client.id}")
    private String googleClientId;

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

        // 4. check quyền login
        if (user.getProvider() == Provider.GOOGLE && user.getPassword() == null) {
            throw new BadRequestException("ACCOUNT_USER_GOOGLE_LOGIN");
        }

        // 4. check password
        if (user.getPassword() == null || !passwordEncoder.matches(password, user.getPassword())) {
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


    public LoginResponse googleLogin(GoogleLoginRequest request) {
        try {
            // 1. Xác thực ID Token với Google
            com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier verifier =
                    new com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier.Builder(
                            new com.google.api.client.http.javanet.NetHttpTransport(),
                            new com.google.api.client.json.gson.GsonFactory())
                            .setAudience(java.util.Collections.singletonList(googleClientId))
                            .build();

            com.google.api.client.googleapis.auth.oauth2.GoogleIdToken idToken = verifier.verify(request.getIdToken());
            if (idToken == null) {
                throw new BadRequestException("INVALID_GOOGLE_TOKEN");
            }

            // 2. Lấy thông tin từ token của Google
            com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail().toLowerCase();
            String googleId = payload.getSubject();
            String name = (String) payload.get("name");

            // 3. Tìm hoặc tạo User mới
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                user = new User();
                user.setEmail(email);
                user.setFullName(name);
                user.setGoogleId(googleId);
                user.setProvider(Provider.GOOGLE);
                user.setRole(Role.CUSTOMER);
                user.setStatus(UserStatus.ACTIVE);
                user.setUsername("user_" + java.util.UUID.randomUUID().toString().substring(0, 8));
                userRepository.save(user);
            } else {
                if (user.getGoogleId() == null) {
                    user.setGoogleId(googleId);
                    user.setProvider(Provider.GOOGLE);
                    userRepository.save(user);
                }
                if (user.getStatus() == UserStatus.BLOCKED) {
                    throw new BadRequestException("ACCOUNT_BLOCKED");
                }
            }

            // 4. Tạo JWT của hệ thống (Nhớ dùng getUsername() như chúng ta đã thống nhất)
            String token = jwtService.generateToken(user.getUsername());

            LoginResponse res = new LoginResponse();
            res.setId(user.getId());
            res.setUsername(user.getUsername());
            res.setEmail(user.getEmail());
            res.setRole(user.getRole().name());
            res.setToken(token);

            return res;

        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException("GOOGLE_AUTHENTICATION_FAILED: " + e.getMessage());
        }
    }
}