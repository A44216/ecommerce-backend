package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.UserRequest;
import com.ecommerce.backend.dto.requests.GoogleLoginRequest;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.enums.Provider;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.dto.responses.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

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
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());

        user.setProvider(Provider.LOCAL);

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

        return res;
    }
}