package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Coupon;
import com.ecommerce.backend.enums.CouponStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Integer> {

    Optional<Coupon> findByCodeAndIsDeletedFalse(String code);

    boolean existsByCodeAndIsDeletedFalse(String code);

    Page<Coupon> findByStatusAndIsDeleted(CouponStatus status, Boolean isDeleted, Pageable pageable);

    Page<Coupon> findByCodeContainingIgnoreCaseAndIsDeleted(String keyword, Boolean isDeleted, Pageable pageable);

    Page<Coupon> findByStatusAndCodeContainingIgnoreCaseAndIsDeleted(
            CouponStatus status,
            String keyword,
            Boolean isDeleted,
            Pageable pageable
    );

    Page<Coupon> findByIsDeleted(Boolean isDeleted, Pageable pageable);
}