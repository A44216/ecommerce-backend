package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Coupon;
import com.ecommerce.backend.enums.CouponStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Integer> {

    Optional<Coupon> findByCodeAndIsDeletedFalse(String code);

    @Modifying
    @Query("UPDATE Coupon c SET c.status = :newStatus WHERE c.endDate < :date AND c.status != :newStatus")
    int updateStatusForExpiredCoupons(
                    @org.springframework.data.repository.query.Param("newStatus") CouponStatus newStatus,
                    @org.springframework.data.repository.query.Param("date") LocalDateTime date);

    boolean existsByCodeAndIsDeletedFalse(String code);

    Page<Coupon> findByStatusAndIsDeleted(CouponStatus status, Boolean isDeleted, Pageable pageable);

    Page<Coupon> findByCodeContainingIgnoreCaseAndIsDeleted(String keyword, Boolean isDeleted, Pageable pageable);

    Page<Coupon> findByStatusAndCodeContainingIgnoreCaseAndIsDeleted(
                    CouponStatus status,
                    String keyword,
                    Boolean isDeleted,
                    Pageable pageable);

    Page<Coupon> findByIsDeleted(Boolean isDeleted, Pageable pageable);

    Long countByStatusAndIsDeletedFalse(CouponStatus status);

    List<Coupon> findTop5ByCodeContainingIgnoreCaseOrderByCodeAsc(String code);

    Optional<Coupon> findByCode(String code);

    boolean existsByCode(String code);

}