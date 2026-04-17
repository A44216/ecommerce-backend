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

    // MAPPER ENTITY -> DTO
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

    // LIST + FILTER + PAGING
    public PageResponse<AdminCouponResponse> getCoupons(
            int page,
            int size,
            CouponStatus status,
            String keyword
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Coupon> coupons;

        if (status != null && keyword != null && !keyword.isEmpty()) {
            coupons = couponRepository.findByStatusAndCodeContainingIgnoreCase(status, keyword, pageable);
        } else if (status != null) {
            coupons = couponRepository.findByStatus(status, pageable);
        } else if (keyword != null && !keyword.isEmpty()) {
            coupons = couponRepository.findByCodeContainingIgnoreCase(keyword, pageable);
        } else {
            coupons = couponRepository.findAll(pageable);
        }

        return new PageResponse<>(
                coupons.getContent().stream().map(this::mapToDTO).toList(),
                coupons.getNumber(),
                coupons.getSize(),
                coupons.getTotalElements(),
                coupons.getTotalPages()
        );
    }

    // GET BY ID
    public AdminCouponResponse getCouponById(Integer id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

        return mapToDTO(coupon);
    }

    // CREATE
    public AdminCouponResponse createCoupon(AdminCouponRequest request) {

        if (couponRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Coupon code already exists");
        }

        Coupon coupon = new Coupon();
        applyRequest(coupon, request);

        validateCoupon(coupon);

        coupon.setStatus(CouponStatus.ACTIVE);
        coupon.setUsedCount(0);
        coupon.setCreatedAt(LocalDateTime.now());
        coupon.setUpdatedAt(LocalDateTime.now());

        return mapToDTO(couponRepository.save(coupon));
    }

    // UPDATE
    public AdminCouponResponse updateCoupon(Integer id, AdminCouponRequest request) {

        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

        if (!coupon.getCode().equals(request.getCode())
                && couponRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Coupon code already exists");
        }

        applyRequest(coupon, request);
        validateCoupon(coupon);

        coupon.setUpdatedAt(LocalDateTime.now());

        return mapToDTO(couponRepository.save(coupon));
    }

    // DELETE
    public void deleteCoupon(Integer id) {
        if (!couponRepository.existsById(id)) {
            throw new ResourceNotFoundException("Coupon not found");
        }
        couponRepository.deleteById(id);
    }

    // DISABLE
    public void disableCoupon(Integer id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

        coupon.setStatus(CouponStatus.DISABLED);
        coupon.setUpdatedAt(LocalDateTime.now());

        couponRepository.save(coupon);
    }

    // ENABLE
    public void enableCoupon(Integer id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

        coupon.setStatus(CouponStatus.ACTIVE);
        coupon.setUpdatedAt(LocalDateTime.now());

        couponRepository.save(coupon);
    }

    // APPLY REQUEST -> ENTITY
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

    // VALIDATION BUSINESS
    private void validateCoupon(Coupon coupon) {

        if (coupon.getDiscountPercent() == null && coupon.getDiscountAmount() == null) {
            throw new BadRequestException("Coupon must have discount value");
        }

        if (coupon.getDiscountPercent() != null && coupon.getDiscountAmount() != null) {
            throw new BadRequestException("Coupon cannot have both percent and amount");
        }

        if (coupon.getMinOrderValue() != null
                && coupon.getMinOrderValue().compareTo(BigDecimal.ZERO) < 0) {
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

    private CouponStatus resolveStatus(Coupon coupon) {

        if (coupon.getStatus() == CouponStatus.DISABLED) {
            return CouponStatus.DISABLED;
        }

        if (coupon.getEndDate() != null
                && coupon.getEndDate().isBefore(LocalDateTime.now())) {
            return CouponStatus.EXPIRED;
        }

        return CouponStatus.ACTIVE;
    }

}