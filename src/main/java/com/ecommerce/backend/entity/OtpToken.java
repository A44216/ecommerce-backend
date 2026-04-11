package com.ecommerce.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_tokens")
@Data // Tự động tạo Getter/Setter
@NoArgsConstructor
public class OtpToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 6)
    private String otp;

    @Column(nullable = false)
    private LocalDateTime expiryDate; // Thời gian hết hạn

    public OtpToken(String email, String otp, LocalDateTime expiryDate) {
        this.email = email;
        this.otp = otp;
        this.expiryDate = expiryDate;
    }
}