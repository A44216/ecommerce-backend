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

    @Query("""
        SELECT\s
        CASE\s
            WHEN LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN u.email
            WHEN LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN u.username
            WHEN u.phone LIKE CONCAT('%', :keyword, '%') THEN u.phone
            ELSE u.fullName
        END
        FROM User u
        WHERE u.role <> 'ADMIN'
        AND (
            :keyword IS NULL OR :keyword = '' OR
            LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            u.phone LIKE CONCAT('%', :keyword, '%')
        )
        ORDER BY
        CASE
            WHEN LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN 1
            WHEN LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN 2
            WHEN u.phone LIKE CONCAT('%', :keyword, '%') THEN 3
            WHEN LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN 4
            ELSE 5
        END,
        u.fullName ASC
   \s""")
    List<String> autocompleteUsers(@Param("keyword") String keyword);

}