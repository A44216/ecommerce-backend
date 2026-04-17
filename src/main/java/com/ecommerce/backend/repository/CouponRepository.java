package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Coupon;
import com.ecommerce.backend.enums.CouponStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Integer> {

    Optional<Coupon> findByCode(String code);

    boolean existsByCode(String code);

    Page<Coupon> findByStatus(CouponStatus status, Pageable pageable);

    Page<Coupon> findByCodeContainingIgnoreCase(String keyword, Pageable pageable);

    Page<Coupon> findByStatusAndCodeContainingIgnoreCase(
            CouponStatus status,
            String keyword,
            Pageable pageable
    );
}