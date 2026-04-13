package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.requests.LoginRequest;
import com.ecommerce.backend.dto.requests.UserRequest;
import com.ecommerce.backend.dto.requests.GoogleLoginRequest;
import com.ecommerce.backend.dto.responses.UserResponse;
import com.ecommerce.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // GET ALL USERS
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    // GET USER BY ID
    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Integer id) {
        return userService.getUserById(id);
    }

    // CREATE USER
    @PostMapping
    public UserResponse createUser(@Valid @RequestBody UserRequest request) {
        return userService.createUser(request);
    }

    // UPDATE USER
    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Integer id,
                                   @Valid @RequestBody UserRequest request) {
        return userService.updateUser(id, request);
    }

    // DELETE USER
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
    }

    // LOGIN NORMAL
    @PostMapping("/login")
    public UserResponse login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }

    // GOOGLE LOGIN
    @PostMapping("/google-login")
    public UserResponse loginGoogle(@RequestBody GoogleLoginRequest request) {
        return userService.loginWithGoogle(request);
    }
}