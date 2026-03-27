package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.requests.LoginRequest;
import com.ecommerce.backend.dto.requests.ResetPasswordRequest;
import com.ecommerce.backend.dto.requests.UserRequest;
import com.ecommerce.backend.dto.responses.LoginResponse;
import com.ecommerce.backend.dto.responses.UserResponse;
import com.ecommerce.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.backend.dto.requests.GoogleLoginRequest;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public UserResponse register(@RequestBody UserRequest request) {
        return authService.register(request);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/google")
    public LoginResponse googleLogin(@RequestBody com.ecommerce.backend.dto.requests.GoogleLoginRequest request) {
        return authService.googleLogin(request);
    }
}