package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.requests.*;

import com.ecommerce.backend.dto.requests.admin.profile.AdminChangePasswordRequest;

import com.ecommerce.backend.dto.responses.LoginResponse;
import com.ecommerce.backend.dto.responses.UserResponse;
import com.ecommerce.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

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

    @PostMapping("/send-register-otp")
    public ResponseEntity<?> sendRegisterOtp(@RequestBody SendOtpRequest request) {
        authService.sendRegisterOtp(request);

        // Trả về JSON báo thành công cho Android biết
        return ResponseEntity.ok(Collections.singletonMap("message", "OTP_SENT_SUCCESSFULLY"));
    }

    @PostMapping("/send-forgot-password-otp")
    public ResponseEntity<?> sendForgotPasswordOtp(@RequestBody SendOtpRequest request) {
        authService.sendForgotPasswordOtp(request);
        return ResponseEntity.ok(Collections.singletonMap("message", "OTP_SENT_SUCCESSFULLY"));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {
        authService.verifyOtp(request);
        return ResponseEntity.ok(Collections.singletonMap("message", "OTP_VALID"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody AdminChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok(Collections.singletonMap("message", "PASSWORD_CHANGED_SUCCESSFULLY"));
    }

    @PostMapping("/send-unlink-email-otp")
    public ResponseEntity<?> sendUnlinkEmailOtp(@RequestBody SendOtpRequest request) {
        authService.sendUnlinkEmailOtp(request);
        return ResponseEntity.ok(Collections.singletonMap("message", "OTP_SENT_SUCCESSFULLY"));
    }

    @PostMapping("/send-verify-new-email-otp")
    public ResponseEntity<?> sendVerifyNewEmailOtp(@RequestBody SendOtpRequest request) {
        authService.sendVerifyNewEmailOtp(request);
        return ResponseEntity.ok(Collections.singletonMap("message", "OTP_SENT_SUCCESSFULLY"));
    }
}