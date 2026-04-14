package com.ecommerce.backend.repository; // Nhớ đổi tên package

import com.ecommerce.backend.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    // Tìm OTP dựa theo email và mã code để xác thực
    Optional<OtpToken> findByEmailAndOtp(String email, String otp);

    // Xóa toàn bộ OTP cũ của một email (trước khi gửi mã mới)
    void deleteByEmail(String email);
}