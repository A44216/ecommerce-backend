package com.ecommerce.backend.entity;

import com.ecommerce.backend.enums.Provider;
import com.ecommerce.backend.enums.Role;
import com.ecommerce.backend.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_users_username", columnNames = "username"),
                @UniqueConstraint(name = "unique_users_email", columnNames = "email"),
                @UniqueConstraint(name = "unique_users_phone", columnNames = "phone"),
                @UniqueConstraint(name = "unique_users_google_id", columnNames = "google_id")
        }
)
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(length = 255)
    private String password;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.CUSTOMER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "google_id", length = 255, unique = true)
    private String googleId;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Provider provider = Provider.LOCAL;

    @Column(name = "avatar")
    private String avatar;

}