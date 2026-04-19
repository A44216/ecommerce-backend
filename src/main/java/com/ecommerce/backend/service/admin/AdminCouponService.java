package com.ecommerce.backend.service.admin;

import com.ecommerce.backend.dto.requests.admin.profile.AdminCouponRequest;
import com.ecommerce.backend.dto.responses.admin.profile.AdminCouponResponse;
import com.ecommerce.backend.dto.responses.seller.PageResponse;
import com.ecommerce.backend.entity.Coupon;
import com.ecommerce.backend.enums.CouponStatus;
import com.ecommerce.backend.exception.BadRequestException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.CouponRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class AdminCouponService {

    private final CouponRepository couponRepository;

    public AdminCouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    // LIST
    public PageResponse<AdminCouponResponse> getCoupons(
            int page,
            int size,
            CouponStatus status,
            String keyword,
            Boolean isDeleted) {
        if (isDeleted == null) {
            isDeleted = false;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Coupon> coupons;

        if (status != null && keyword != null && !keyword.isEmpty()) {
            coupons = couponRepository.findByStatusAndCodeContainingIgnoreCaseAndIsDeleted(
                    status, keyword, isDeleted, pageable);

        } else if (status != null) {
            coupons = couponRepository.findByStatusAndIsDeleted(status, isDeleted, pageable);

        } else if (keyword != null && !keyword.isEmpty()) {
            coupons = couponRepository.findByCodeContainingIgnoreCaseAndIsDeleted(keyword, isDeleted, pageable);

        } else {
            coupons = couponRepository.findByIsDeleted(isDeleted, pageable);
        }

        return new PageResponse<>(
                coupons.getContent().stream().map(this::mapToDTO).toList(),
                coupons.getNumber(),
                coupons.getSize(),
                coupons.getTotalElements(),
                coupons.getTotalPages());
    }

    // GET BY ID
    public AdminCouponResponse getCouponById(Integer id, Boolean isDeleted) {
        if (isDeleted == null) {
            isDeleted = false;
        }

        final boolean finalIsDeleted = isDeleted;
        Coupon coupon = couponRepository.findById(id)
                .filter(c -> Boolean.TRUE.equals(c.getIsDeleted()) == finalIsDeleted)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

        return mapToDTO(coupon);
    }

    // CREATE
    public AdminCouponResponse createCoupon(AdminCouponRequest request) {

        if (couponRepository.existsByCodeAndIsDeletedFalse(request.getCode())) {
            throw new BadRequestException("Coupon code already exists");
        }

        Coupon coupon = new Coupon();
        applyRequest(coupon, request);
        validateCoupon(coupon);

        if (coupon.getEndDate() != null && coupon.getEndDate().isBefore(LocalDateTime.now())) {
            coupon.setStatus(CouponStatus.EXPIRED);
        } else {
            coupon.setStatus(CouponStatus.ACTIVE);
        }

        coupon.setUsedCount(0);
        coupon.setIsDeleted(false);
        coupon.setCreatedAt(LocalDateTime.now());
        coupon.setUpdatedAt(LocalDateTime.now());

        return mapToDTO(couponRepository.save(coupon));
    }

    // UPDATE
    public AdminCouponResponse updateCoupon(Integer id, AdminCouponRequest request) {

        Coupon coupon = couponRepository.findById(id)
                .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

        if (!coupon.getCode().equals(request.getCode())
                && couponRepository.existsByCodeAndIsDeletedFalse(request.getCode())) {
            throw new BadRequestException("Coupon code already exists");
        }

        boolean wasExpiredByDate = coupon.getEndDate() != null && coupon.getEndDate().isBefore(LocalDateTime.now());
        boolean wasStatusExpired = coupon.getStatus() == CouponStatus.EXPIRED;

        applyRequest(coupon, request);
        validateCoupon(coupon);

        boolean isNowExpiredByDate = coupon.getEndDate() != null && coupon.getEndDate().isBefore(LocalDateTime.now());

        if (isNowExpiredByDate) {
            coupon.setStatus(CouponStatus.EXPIRED);
        } else if (wasStatusExpired || wasExpiredByDate) {
            coupon.setStatus(CouponStatus.ACTIVE);
        }

        coupon.setUpdatedAt(LocalDateTime.now());

        return mapToDTO(couponRepository.save(coupon));
    }

    // SOFT DELETE
    public void deleteCoupon(Integer id) {

        Coupon coupon = couponRepository.findById(id)
                .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

        coupon.setIsDeleted(true);
        coupon.setUpdatedAt(LocalDateTime.now());

        couponRepository.save(coupon);
    }

    // RESTORE
    public void restoreCoupon(Integer id) {

        Coupon coupon = couponRepository.findById(id)
                .filter(c -> Boolean.TRUE.equals(c.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Deleted coupon not found"));

        coupon.setIsDeleted(false);
        coupon.setUpdatedAt(LocalDateTime.now());

        couponRepository.save(coupon);
    }

    // ENABLE
    public void enableCoupon(Integer id) {

        Coupon coupon = couponRepository.findById(id)
                .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

        if (coupon.getEndDate() != null && coupon.getEndDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Cannot enable an expired coupon");
        }

        coupon.setStatus(CouponStatus.ACTIVE);
        coupon.setUpdatedAt(LocalDateTime.now());

        couponRepository.save(coupon);
    }

    // DISABLE
    public void disableCoupon(Integer id) {

        Coupon coupon = couponRepository.findById(id)
                .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

        if (coupon.getEndDate() != null && coupon.getEndDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Cannot disable an expired coupon");
        }

        coupon.setStatus(CouponStatus.DISABLED);
        coupon.setUpdatedAt(LocalDateTime.now());

        couponRepository.save(coupon);
    }

    // VALIDATION
    private void validateCoupon(Coupon coupon) {

        if (coupon.getDiscountPercent() == null && coupon.getDiscountAmount() == null) {
            throw new BadRequestException("Coupon must have discount value");
        }

        if (coupon.getDiscountPercent() != null && coupon.getDiscountAmount() != null) {
            throw new BadRequestException("Coupon cannot have both percent and amount");
        }

        if (coupon.getMinOrderValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("minOrderValue must be >= 0");
        }

        if (coupon.getMaxDiscountAmount() != null
                && coupon.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("maxDiscountAmount must be >= 0");
        }

        if (coupon.getStartDate() != null && coupon.getEndDate() != null
                && coupon.getEndDate().isBefore(coupon.getStartDate())) {
            throw new BadRequestException("endDate must be after startDate");
        }
    }

    // MAPPER
    private AdminCouponResponse mapToDTO(Coupon coupon) {
        return AdminCouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .discountPercent(coupon.getDiscountPercent())
                .discountAmount(coupon.getDiscountAmount())
                .minOrderValue(coupon.getMinOrderValue())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .startDate(coupon.getStartDate())
                .endDate(coupon.getEndDate())
                .maxUsage(coupon.getMaxUsage())
                .usedCount(coupon.getUsedCount())
                .status(resolveStatus(coupon))
                .createdAt(coupon.getCreatedAt())
                .updatedAt(coupon.getUpdatedAt())
                .build();
    }

    // STATUS LOGIC
    private CouponStatus resolveStatus(Coupon coupon) {

        if (Boolean.TRUE.equals(coupon.getIsDeleted())) {
            return CouponStatus.DISABLED;
        }

        if (coupon.getEndDate() != null
                && coupon.getEndDate().isBefore(LocalDateTime.now())) {
            return CouponStatus.EXPIRED;
        }

        if (coupon.getStatus() == CouponStatus.DISABLED) {
            return CouponStatus.DISABLED;
        }


        return CouponStatus.ACTIVE;
    }

    // APPLY REQUEST
    private void applyRequest(Coupon coupon, AdminCouponRequest request) {
        coupon.setCode(request.getCode());
        coupon.setDiscountPercent(request.getDiscountPercent());
        coupon.setDiscountAmount(request.getDiscountAmount());
        coupon.setMinOrderValue(request.getMinOrderValue());
        coupon.setMaxDiscountAmount(request.getMaxDiscountAmount());
        coupon.setStartDate(request.getStartDate());
        coupon.setEndDate(request.getEndDate());
        coupon.setMaxUsage(request.getMaxUsage());
    }
}