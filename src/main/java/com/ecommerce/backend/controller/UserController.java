package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.requests.UserRequest;
import com.ecommerce.backend.dto.requests.GoogleLoginRequest;
import com.ecommerce.backend.dto.responses.UserResponse;
import com.ecommerce.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Integer id) {
        return userService.getUserById(id);
    }

    @PostMapping
    public UserResponse createUser(@RequestBody UserRequest request) {
        return userService.createUser(request);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Integer id, @RequestBody UserRequest request) {
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
}