package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.*;
import com.ecommerce.backend.dto.requests.admin.profile.AdminChangePasswordRequest;

import com.ecommerce.backend.dto.responses.LoginResponse;
import com.ecommerce.backend.dto.responses.UserResponse;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.enums.UserStatus;
import com.ecommerce.backend.exception.BadRequestException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecommerce.backend.repository.OtpTokenRepository;
import com.ecommerce.backend.entity.OtpToken;


import com.ecommerce.backend.enums.Provider;
import com.ecommerce.backend.enums.Role;

import java.util.Objects;

import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    @org.springframework.beans.factory.annotation.Value("${google.client.id}")
    private String googleClientId;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;

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
    @Transactional
    public UserResponse register(UserRequest request) {
        String username = request.getUsername() != null ? request.getUsername().trim() : null;
        String email = request.getEmail() != null ? request.getEmail().trim() : null;
        String fullName = request.getFullName() != null ? request.getFullName().trim() : null;
        String otpCode = request.getOtpCode();
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
        if (otpCode == null || otpCode.trim().isEmpty()) {
            throw new BadRequestException("OTP_REQUIRED");
        }
        OtpToken validOtp = otpTokenRepository.findByEmailAndOtp(email, otpCode.trim())
                .orElseThrow(() -> new BadRequestException("INVALID_OTP"));

        if (validOtp.getExpiryDate().isBefore(LocalDateTime.now())) {
            otpTokenRepository.delete(validOtp); // Xóa OTP đã hết hạn
            throw new BadRequestException("OTP_EXPIRED");
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
        user.setProvider(Provider.LOCAL);
        userRepository.save(user);

        // Đăng ký thành công thì xóa OTP đi để tránh dùng lại
        otpTokenRepository.deleteByEmail(email);

        return mapToResponse(user);
    }
    // 3. HÀM ĐỔI MẬT KHẨU
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String input = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null;
        String newPassword = request.getNewPassword();

        if (input == null || input.isEmpty()) throw new BadRequestException("INVALID_EMAIL");
        if (newPassword == null || newPassword.trim().length() < 6) throw new BadRequestException("INVALID_PASSWORD");

        // Tìm user bằng email hoặc username
        User user;
        if (input.contains("@")) {
            user = userRepository.findByEmail(input).orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND"));
        } else {
            user = userRepository.findByUsername(input).orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND"));
        }

        user.setPassword(passwordEncoder.encode(newPassword.trim()));
        userRepository.save(user);
        otpTokenRepository.deleteByEmail(user.getEmail()); // Xóa rác OTP bằng Email thật
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
                // BỔ SUNG: Chặn việc ghi đè tài khoản LOCAL
                if (user.getProvider() == Provider.LOCAL) {
                    throw new BadRequestException("EMAIL_ALREADY_REGISTERED_LOCAL");
                }

                // Nếu user đã tồn tại và provider là GOOGLE thì cho đi tiếp (đăng nhập bình thường)
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

    @Transactional
    public void sendRegisterOtp(SendOtpRequest request) {
        String email = request.getEmail();
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("INVALID_EMAIL");
        }
        email = email.trim().toLowerCase();

        // 1. Kiểm tra xem Email đã có ai đăng ký chưa (tránh trùng lặp)
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("EMAIL_ALREADY_EXISTS");
        }

        // 2. Dọn dẹp rác: Xóa các mã OTP cũ của email này
        otpTokenRepository.deleteByEmail(email);

        // 3. Sinh mã OTP ngẫu nhiên 6 chữ số
        String otpCode = String.format("%06d", new Random().nextInt(999999));

        // 4. Lưu mã OTP vào Database (Cài đặt hết hạn sau 5 phút)
        OtpToken otpToken = new OtpToken(
                email,
                otpCode,
                LocalDateTime.now().plusMinutes(5)
        );
        otpTokenRepository.save(otpToken);

        // 5. Gửi Email
        emailService.sendOtpEmail(email, otpCode);
    }
    // 1. HÀM GỬI MÃ
    @Transactional
    public void sendForgotPasswordOtp(SendOtpRequest request) {
        String input = request.getEmail().trim().toLowerCase();

        User user;
        if (input.contains("@")) {
            user = userRepository.findByEmail(input).orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND"));
        } else {
            user = userRepository.findByUsername(input).orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND"));
        }

        if (user.getPassword() == null) {
            throw new BadRequestException("GOOGLE_ACCOUNT_NO_PASSWORD");
        }

        String targetEmail = user.getEmail();
        otpTokenRepository.deleteByEmail(targetEmail);

        String otpCode = String.format("%06d", new Random().nextInt(999999));
        OtpToken otpToken = new OtpToken(targetEmail, otpCode, LocalDateTime.now().plusMinutes(5));
        otpTokenRepository.save(otpToken);

        // Lát nữa chúng ta sẽ đổi tên hàm này ở Vấn đề 2
        emailService.sendForgotPasswordEmail(targetEmail, otpCode);
    }

    // 2. HÀM XÁC NHẬN MÃ
    public void verifyOtp(VerifyOtpRequest request) {
        String input = request.getEmail().trim().toLowerCase(); // Chứa email hoặc username
        String otpCode = request.getOtpCode().trim();

        // Dịch từ Username sang Email thật (nếu cần)
        String targetEmail = input;
        if (!input.contains("@")) {
            User user = userRepository.findByUsername(input).orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND"));
            targetEmail = user.getEmail();
        }

        OtpToken validOtp = otpTokenRepository.findByEmailAndOtp(targetEmail, otpCode)
                .orElseThrow(() -> new BadRequestException("INVALID_OTP"));

        if (validOtp.getExpiryDate().isBefore(LocalDateTime.now())) {
            otpTokenRepository.delete(validOtp);
            throw new BadRequestException("OTP_EXPIRED");
        }
    }

    @Transactional
    public void changePassword(AdminChangePasswordRequest request) {

        User user = getCurrentUser();

        if (request.getCurrentPassword() == null || request.getNewPassword() == null) {
            throw new BadRequestException("INVALID_INPUT");
        }

        // check mật khẩu cũ
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("CURRENT_PASSWORD_INCORRECT");
        }

        // check new password
        if (request.getNewPassword().length() < 6) {
            throw new BadRequestException("INVALID_PASSWORD");
        }

        // update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private User getCurrentUser() {
        String username = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND"));
    }

    public void sendUnlinkEmailOtp(SendOtpRequest request) {
        String input = request.getEmail().trim().toLowerCase();

        User user;
        if (input.contains("@")) {
            user = userRepository.findByEmail(input).orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND"));
        } else {
            user = userRepository.findByUsername(input).orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND"));
        }

        if (user.getPassword() == null) {
            throw new BadRequestException("GOOGLE_ACCOUNT_NO_PASSWORD");
        }

        String targetEmail = user.getEmail();
        otpTokenRepository.deleteByEmail(targetEmail);

        String otpCode = String.format("%06d", new Random().nextInt(999999));
        OtpToken otpToken = new OtpToken(targetEmail, otpCode, LocalDateTime.now().plusMinutes(5));
        otpTokenRepository.save(otpToken);

        emailService.sendUnlinkEmailOtp(targetEmail, otpCode);
    }

    @Transactional
    public void sendVerifyNewEmailOtp(SendOtpRequest request) {
        String email = request.getEmail();
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("INVALID_EMAIL");
        }
        email = email.trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("EMAIL_ALREADY_EXISTS");
        }

        otpTokenRepository.deleteByEmail(email);

        String otpCode = String.format("%06d", new Random().nextInt(999999));
        OtpToken otpToken = new OtpToken(email, otpCode, LocalDateTime.now().plusMinutes(5));
        otpTokenRepository.save(otpToken);

        emailService.sendVerifyNewEmailOtp(email, otpCode);
    }

}