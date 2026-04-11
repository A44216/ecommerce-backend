package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.requests.LoginRequest;
import com.ecommerce.backend.dto.requests.UserRequest;
import com.ecommerce.backend.dto.requests.GoogleLoginRequest;
import com.ecommerce.backend.dto.responses.ShopResponse;
import com.ecommerce.backend.dto.responses.UserResponse;
import com.ecommerce.backend.service.ShopService;
import com.ecommerce.backend.service.UserService;
import com.ecommerce.backend.dto.requests.UpdateProfileRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.backend.service.FileStorageService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ShopService shopService;
    private final FileStorageService fileStorageService;
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Integer id) {
        return userService.getUserById(id);
    }

    @PostMapping
    public UserResponse createUser(@Valid @RequestBody UserRequest request) {
        return userService.createUser(request);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Integer id, @Valid @RequestBody UserRequest request) {
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
    }

    // API GOOGLE LOGIN
    @PostMapping("/google-login")
    public UserResponse loginGoogle(@RequestBody GoogleLoginRequest request) {
        return userService.loginWithGoogle(request);
    }

    @PostMapping("/login")
    public UserResponse login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @GetMapping("/user/{userId}")
    public ShopResponse getShopByUser(@PathVariable Integer userId) {
        return shopService.getShopByUser(userId);
    }


    @PostMapping("/{id}/avatar")
    public UserResponse uploadAvatar(@PathVariable Integer id, @RequestParam("file") MultipartFile file) {
        // 1. Lưu file vật lý vào máy
        String fileName = fileStorageService.storeFile(file);

        // 2. Tạo link URL để truy cập ảnh (Ví dụ: http://10.0.2.2:8080/api/images/abc.jpg)
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/images/")
                .path(fileName)
                .toUriString();

        // 3. Lưu link URL đó vào bảng Users trong Database
        return userService.updateUserAvatar(id, fileDownloadUri);
    }

    @PutMapping("/{id}/profile")
    public UserResponse updateProfile(@PathVariable Integer id, @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(id, request);
    }
}