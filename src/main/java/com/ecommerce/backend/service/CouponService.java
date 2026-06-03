package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.CouponRequest;
import com.ecommerce.backend.dto.responses.CouponResponse;
import com.ecommerce.backend.entity.Coupon;
import com.ecommerce.backend.enums.CouponStatus;
import com.ecommerce.backend.enums.NotificationType;
import com.ecommerce.backend.exception.BadRequestException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class CouponService {

    private final CouponRepository couponRepository;
    private final NotificationService notificationService;
    private final com.ecommerce.backend.repository.OrderRepository orderRepository;
    private final com.ecommerce.backend.util.SecurityUtils securityUtils;

    public CouponService(CouponRepository couponRepository, NotificationService notificationService,
                         com.ecommerce.backend.repository.OrderRepository orderRepository,
                         com.ecommerce.backend.util.SecurityUtils securityUtils) {
        this.couponRepository = couponRepository;
        this.notificationService = notificationService;
        this.orderRepository = orderRepository;
        this.securityUtils = securityUtils;
    }

    // MAPPER: Entity -> DTO
    private CouponResponse mapToDTO(Coupon coupon) {
        return CouponResponse.builder()
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
                .orElseThrow(() -> new ResourceNotFoundException("Mã giảm giá không tồn tại"));
                
        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            throw new BadRequestException("Mã giảm giá không hợp lệ hoặc đã hết hạn");
        }
        
        try {
            Integer userId = securityUtils.getCurrentUserId();
            if (userId != null) {
                boolean isUsed = orderRepository.existsByCouponIdAndUserIdAndStatusNot(
                        coupon.getId(), userId, com.ecommerce.backend.enums.OrderStatus.CANCELED);
                if (isUsed) {
                    throw new BadRequestException("Bạn đã sử dụng mã này rồi");
                }
            }
        } catch (Exception e) {
            // Nếu không lấy được user (chưa đăng nhập hoặc lỗi gì đó), bỏ qua check hoặc ném lỗi
            // Vì chỉ user mới apply coupon được
            if (e instanceof BadRequestException) {
                throw e; // Re-throw the "Bạn đã sử dụng mã này rồi" exception
            }
        }

        return mapToDTO(coupon);
    }

    // tạo mã mới
    @Transactional
    public CouponResponse createCoupon(CouponRequest request) {
        if (couponRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Coupon code already exists");
        }

        Coupon coupon = new Coupon();
        mapRequestToCoupon(coupon, request);
        coupon.setStatus(CouponStatus.ACTIVE);
        coupon.setUsedCount(0); // Mã mới chưa có ai dùng

        Coupon savedCoupon = couponRepository.save(coupon);

        // Gửi thông báo đến người dùng
        String title = "Mã giảm giá mới: " + savedCoupon.getCode();
        StringBuilder bodyBuilder = new StringBuilder("Nhập mã " + savedCoupon.getCode());
        
        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        
        if (savedCoupon.getDiscountPercent() != null) {
            bodyBuilder.append(" giảm ").append(savedCoupon.getDiscountPercent()).append("%");
        } else if (savedCoupon.getDiscountAmount() != null) {
            bodyBuilder.append(" giảm ").append(currencyFormat.format(savedCoupon.getDiscountAmount())).append("đ");
        }
        
        if (savedCoupon.getMinOrderValue() != null && savedCoupon.getMinOrderValue().doubleValue() > 0) {
            bodyBuilder.append(" cho đơn từ ").append(currencyFormat.format(savedCoupon.getMinOrderValue())).append("đ.");
        } else {
            bodyBuilder.append(".");
        }
        
        if (savedCoupon.getEndDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            bodyBuilder.append(" Hạn sử dụng: ").append(savedCoupon.getEndDate().format(formatter)).append(".");
        }
        
        notificationService.broadcastNotification(title, bodyBuilder.toString(), NotificationType.PROMOTION);

        return mapToDTO(savedCoupon);
    }

    // cập nhật mã
    @Transactional
    public CouponResponse updateCoupon(Integer id, CouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

        // Nếu đổi code sang code khác, phải check xem code mới đã tồn tại chưa
        if (!coupon.getCode().equals(request.getCode()) && couponRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Coupon code already exists");
        }

        mapRequestToCoupon(coupon, request);
        return mapToDTO(couponRepository.save(coupon));
    }

    // xóa mã
    @Transactional
    public void deleteCoupon(Integer id) {
        if (!couponRepository.existsById(id)) {
            throw new ResourceNotFoundException("Coupon not found");
        }
        couponRepository.deleteById(id);
    }
}