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

    Page<Coupon> findByStatusAndIsDeletedFalse(CouponStatus status, Pageable pageable);

    Page<Coupon> findByCodeContainingIgnoreCaseAndIsDeletedFalse(String keyword, Pageable pageable);

    Page<Coupon> findByStatusAndCodeContainingIgnoreCaseAndIsDeletedFalse(
            CouponStatus status,
            String keyword,
            Pageable pageable
    );

    Page<Coupon> findByIsDeletedFalse(Pageable pageable);
}