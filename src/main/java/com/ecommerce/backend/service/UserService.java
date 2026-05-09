package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.LoginRequest;
import com.ecommerce.backend.dto.requests.UserRequest;
import com.ecommerce.backend.dto.requests.GoogleLoginRequest;
import com.ecommerce.backend.dto.requests.*;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.ecommerce.backend.entity.OtpToken;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    private final com.ecommerce.backend.repository.OtpTokenRepository otpTokenRepository;

    // Helper map dữ liệu sang DTO (Dùng chung cho tất cả các hàm trả về UserResponse)
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

     // Bổ sung các cờ trạng thái quan trọng cho Frontend
        res.setHasPassword(user.getPassword() != null && !user.getPassword().trim().isEmpty());

        // CHỐT CHẶN: Kiểm tra chính xác Enum Provider
        res.setGoogleAccount(user.getProvider() != null && user.getProvider() == Provider.GOOGLE);

        return res;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public UserResponse getUserById(Integer id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToResponse(user);
    }

    @Transactional
    public UserResponse createUser(UserRequest request) {
        User user = new User();
        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setProvider(Provider.LOCAL);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        return mapToResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Integer id, UserRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        userRepository.save(user);
        return mapToResponse(user);
    }

    @Transactional
    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public UserResponse loginWithGoogle(GoogleLoginRequest request) {
        var userOpt = userRepository.findByGoogleId(request.getGoogleId());
        if (userOpt.isPresent()) {
            return mapToResponse(userOpt.get());
        }
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
            user.setUsername(generateUsername(request.getEmail()));
            user.setPassword(null);
        }
        userRepository.save(user);
        return mapToResponse(user);
    }

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

    public UserResponse login(LoginRequest request) {
        if (request.getPassword() == null || (request.getUsername() == null && request.getEmail() == null)) {
            throw new BadRequestException("Thiếu username/email hoặc password");
        }
        User user;
        if (request.getUsername() != null) {
            user = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));
        } else {
            user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));
        }
        if (user.getStatus() == UserStatus.BLOCKED) throw new BadRequestException("Tài khoản đã bị khóa");
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) throw new BadRequestException("Mật khẩu không đúng");
        return mapToResponse(user);
    }

    @Transactional
    public UserResponse updateUserAvatar(Integer id, String avatarUrl) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setAvatar(avatarUrl);
        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateProfile(Integer id, UpdateProfileRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());

        // CHẶN ĐỔI EMAIL CHO TÀI KHOẢN GOOGLE TẠI SERVER
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (user.getProvider() == Provider.GOOGLE) {
                throw new BadRequestException("Tài khoản Google không thể thay đổi Email!");
            }
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new BadRequestException("Email đã tồn tại!");
            }
            user.setEmail(request.getEmail());
        }

        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    public void changePassword(Integer userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        OtpToken validOtp = otpTokenRepository.findByEmailAndOtp(user.getEmail(), request.getOtpCode().trim())
                .orElseThrow(() -> new RuntimeException("Mã xác thực không chính xác"));
        if (validOtp.getExpiryDate().isBefore(LocalDateTime.now())) {
            otpTokenRepository.delete(validOtp);
            throw new RuntimeException("Mã xác thực đã hết hạn");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword().trim()));
        userRepository.save(user);
        otpTokenRepository.deleteByEmail(user.getEmail());
    }

    @Transactional
    public void setPasswordForGoogleAccount(Integer userId, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void changeEmail(Integer userId, ChangeEmailRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (user.getProvider() == Provider.GOOGLE) {
            throw new BadRequestException("Tài khoản Google không thể thay đổi Email!");
        }
        
        String newEmail = request.getNewEmail().trim().toLowerCase();
        if (userRepository.findByEmail(newEmail).isPresent()) {
            throw new BadRequestException("Email đã tồn tại!");
        }

        // Kiểm tra OTP email cũ
        OtpToken oldOtp = otpTokenRepository.findByEmailAndOtp(user.getEmail(), request.getOldEmailOtp().trim())
                .orElseThrow(() -> new BadRequestException("Mã OTP xác thực email cũ không chính xác"));
        if (oldOtp.getExpiryDate().isBefore(LocalDateTime.now())) {
            otpTokenRepository.delete(oldOtp);
            throw new BadRequestException("Mã OTP email cũ đã hết hạn");
        }

        // Kiểm tra OTP email mới
        OtpToken newOtp = otpTokenRepository.findByEmailAndOtp(newEmail, request.getNewEmailOtp().trim())
                .orElseThrow(() -> new BadRequestException("Mã OTP xác thực email mới không chính xác"));
        if (newOtp.getExpiryDate().isBefore(LocalDateTime.now())) {
            otpTokenRepository.delete(newOtp);
            throw new BadRequestException("Mã OTP email mới đã hết hạn");
        }

        // Cập nhật
        user.setEmail(newEmail);
        userRepository.save(user);

        // Xóa OTP
        otpTokenRepository.delete(oldOtp);
        otpTokenRepository.delete(newOtp);
    }
}