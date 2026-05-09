package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.enums.Role;
import com.ecommerce.backend.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByGoogleId(String googleId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    List<User> findByRole(Role role);

    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' AND u.role <> 'ADMIN'")
    List<User> findActiveUsers();

    @Query("SELECT u FROM User u WHERE " +
           "u.role <> 'ADMIN' AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(:keyword IS NULL OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> searchUsers(@Param("role") Role role, @Param("status") UserStatus status, @Param("keyword") String keyword, Pageable pageable);

    @Query(value = """
        SELECT value FROM (
            (
                SELECT u.email AS value, 1 AS priority
                FROM users u
                WHERE u.role <> 'ADMIN'
                AND LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                ORDER BY u.created_at DESC
                LIMIT 5
            )
            UNION ALL
            (
                SELECT u.username AS value, 2 AS priority
                FROM users u
                WHERE u.role <> 'ADMIN'
                AND LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                ORDER BY u.created_at DESC
                LIMIT 5
            )
            UNION ALL
            (
                SELECT u.phone AS value, 3 AS priority
                FROM users u
                WHERE u.role <> 'ADMIN'
                AND u.phone LIKE CONCAT('%', :keyword, '%')
                ORDER BY u.created_at DESC
                LIMIT 5
            )
            UNION ALL
            (
                SELECT u.full_name AS value, 4 AS priority
                FROM users u
                WHERE u.role <> 'ADMIN'
                AND LOWER(u.full_name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                ORDER BY u.created_at DESC
                LIMIT 5
            )
        ) t
        ORDER BY priority
        LIMIT 5
    """, nativeQuery = true)
    List<String> autocompleteUsers(@Param("keyword") String keyword);

}