package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.LoginRequest;
import com.ecommerce.backend.dto.requests.UserRequest;
import com.ecommerce.backend.dto.requests.GoogleLoginRequest;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.enums.Provider;
import com.ecommerce.backend.enums.UserStatus;
import com.ecommerce.backend.exception.BadRequestException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.dto.responses.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return mapToResponse(user);
    }

    public UserResponse createUser(UserRequest request) {

        User user = new User();

        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());

        user.setProvider(Provider.LOCAL);

        // Mã hóa mật khẩu
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        return mapToResponse(user);
    }

    public UserResponse updateUser(Integer id, UserRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());

        userRepository.save(user);

        return mapToResponse(user);
    }

    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    // GOOGLE LOGIN
    public UserResponse loginWithGoogle(GoogleLoginRequest request) {

        // 1. check google_id
        var userOpt = userRepository.findByGoogleId(request.getGoogleId());

        if (userOpt.isPresent()) {
            return mapToResponse(userOpt.get());
        }

        // 2. check email
        var emailUserOpt = userRepository.findByEmail(request.getEmail());

        User user;

        if (emailUserOpt.isPresent()) {
            user = emailUserOpt.get();
            user.setGoogleId(request.getGoogleId());
            user.setProvider(Provider.GOOGLE);
        } else {
            user = new User();
            user.setFullName(request.getName());
            user.setEmail(request.getEmail());
            user.setGoogleId(request.getGoogleId());
            user.setProvider(Provider.GOOGLE);

            // tự sinh username
            user.setUsername(generateUsername(request.getEmail()));

            user.setPassword(null);
        }

        userRepository.save(user);

        return mapToResponse(user);
    }

    // generate username
    private String generateUsername(String email) {
        String base = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "");
        String username = base;
        int count = 0;

        while (userRepository.findByUsername(username).isPresent()) {
            count++;
            username = base + "_" + count;
        }

        return username;
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
        res.setAvatar(user.getAvatar());

        return res;
    }

    // Đăng nhập thường
    public UserResponse login(LoginRequest request) {

        if (request.getPassword() == null ||
                (request.getUsername() == null && request.getEmail() == null)) {
            throw new BadRequestException("Thiếu username/email hoặc password");
        }

        User user;

        if (request.getUsername() != null) {
            user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));
        } else {
            user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));
        }

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new BadRequestException("Tài khoản đã bị khóa");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu không đúng");
        }

        return mapToResponse(user);
    }

    public UserResponse updateAvatar(Integer id, String avatar) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAvatar(avatar);
        userRepository.save(user);

        return mapToResponse(user);
    }

}