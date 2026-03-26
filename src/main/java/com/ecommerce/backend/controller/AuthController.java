package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.requests.UserRequest;
import com.ecommerce.backend.dto.responses.UserResponse;
import com.ecommerce.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public UserResponse login(@RequestBody UserRequest request) {
        return authService.login(request);
    }
}