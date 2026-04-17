package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.CouponRequest;
import com.ecommerce.backend.dto.responses.CouponResponse;
import com.ecommerce.backend.entity.Coupon;
import com.ecommerce.backend.enums.CouponStatus;
import com.ecommerce.backend.exception.BadRequestException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.CouponRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    // MAPPER: Entity -> DTO
    private CouponResponse mapToDTO(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .discountPercent(coupon.getDiscountPercent())
                .discountAmount(coupon.getDiscountAmount())
                .minOrderValue(coupon.getMinOrderValue())
                .startDate(coupon.getStartDate())
                .endDate(coupon.getEndDate())
                .maxUsage(coupon.getMaxUsage())
                .usedCount(coupon.getUsedCount())
                .status(coupon.getStatus())
                .build();
    }

    // MAPPER: DTO -> Entity
    private void mapRequestToCoupon(Coupon coupon, CouponRequest request) {
        coupon.setCode(request.getCode());
        coupon.setDiscountPercent(request.getDiscountPercent());
        coupon.setDiscountAmount(request.getDiscountAmount());
        coupon.setMinOrderValue(request.getMinOrderValue());
        coupon.setStartDate(request.getStartDate());
        coupon.setEndDate(request.getEndDate());
        coupon.setMaxUsage(request.getMaxUsage());
    }

    // lấy tất cả mã giảm giá
    public List<CouponResponse> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    // lấy mã theo ID
    public CouponResponse getCouponById(Integer id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
        return mapToDTO(coupon);
    }

    // lấy mã theo Code (Dùng khi khách hàng nhập mã)
    public CouponResponse getCouponByCode(String code) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon code not found"));
        return mapToDTO(coupon);
    }

    // tạo mã mới
    public CouponResponse createCoupon(CouponRequest request) {
        if (couponRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Coupon code already exists");
        }

        Coupon coupon = new Coupon();
        mapRequestToCoupon(coupon, request);
        validateCoupon(coupon);
        coupon.setStatus(CouponStatus.ACTIVE);
        coupon.setUsedCount(0);

        return mapToDTO(couponRepository.save(coupon));
    }

    // cập nhật mã
    public CouponResponse updateCoupon(Integer id, CouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

        // Nếu đổi code sang code khác, phải check xem code mới đã tồn tại chưa
        if (!coupon.getCode().equals(request.getCode()) && couponRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Coupon code already exists");
        }

        mapRequestToCoupon(coupon, request);
        validateCoupon(coupon);

        return mapToDTO(couponRepository.save(coupon));
    }

    // xóa mã
    public void deleteCoupon(Integer id) {
        if (!couponRepository.existsById(id)) {
            throw new ResourceNotFoundException("Coupon not found");
        }
        couponRepository.deleteById(id);
    }

    private void validateCoupon(Coupon coupon) {

        // 1. check discount type
        if (coupon.getDiscountPercent() == null && coupon.getDiscountAmount() == null) {
            throw new BadRequestException("Coupon must have discount value");
        }

        if (coupon.getDiscountPercent() != null && coupon.getDiscountAmount() != null) {
            throw new BadRequestException("Coupon cannot have both percent and amount");
        }

        // 2. min order value
        if (coupon.getMinOrderValue() != null && coupon.getMinOrderValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("minOrderValue must be >= 0");
        }

        // 3. max discount
        if (coupon.getMaxDiscountAmount() != null && coupon.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("maxDiscountAmount must be >= 0");
        }

        // 4. date logic
        if (coupon.getStartDate() != null && coupon.getEndDate() != null &&
                coupon.getEndDate().isBefore(coupon.getStartDate())) {
            throw new BadRequestException("endDate must be after startDate");
        }
    }

}