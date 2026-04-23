package com.ecommerce.backend.controller.admin;

import com.ecommerce.backend.dto.requests.admin.profile.AdminCouponRequest;
import com.ecommerce.backend.dto.responses.admin.profile.AdminCouponResponse;
import com.ecommerce.backend.dto.responses.seller.PageResponse;
import com.ecommerce.backend.enums.CouponStatus;
import com.ecommerce.backend.service.admin.AdminCouponService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin/coupons")
public class AdminCouponController {

    private final AdminCouponService couponService;

    public AdminCouponController(AdminCouponService couponService) {
        this.couponService = couponService;
    }

    // LIST
    @GetMapping
    public ResponseEntity<PageResponse<AdminCouponResponse>> getCoupons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) CouponStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") Boolean isDeleted) {
        return ResponseEntity.ok(
                couponService.getCoupons(page, size, status, keyword, isDeleted));
    }

    // DETAIL
    @GetMapping("/{id}")
    public ResponseEntity<AdminCouponResponse> getById(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "false") Boolean isDeleted) {
        return ResponseEntity.ok(couponService.getCouponById(id, isDeleted));
    }

    // CREATE
    @PostMapping
    public ResponseEntity<AdminCouponResponse> create(
            @Valid @RequestBody AdminCouponRequest request) {
        return new ResponseEntity<>(
                couponService.createCoupon(request),
                HttpStatus.CREATED);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<AdminCouponResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody AdminCouponRequest request) {
        return ResponseEntity.ok(
                couponService.updateCoupon(id, request));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.noContent().build();
    }

    // RESTORE (khôi phục coupon đã xóa)
    @PatchMapping("/{id}/restore")
    public ResponseEntity<Void> restore(@PathVariable Integer id) {
        couponService.restoreCoupon(id);
        return ResponseEntity.noContent().build();
    }

    // DISABLE (tắt coupon)
    @PatchMapping("/{id}/disable")
    public ResponseEntity<Void> disable(@PathVariable Integer id) {
        couponService.disableCoupon(id);
        return ResponseEntity.noContent().build();
    }

    // ENABLE (bật lại coupon)
    @PatchMapping("/{id}/enable")
    public ResponseEntity<Void> enable(@PathVariable Integer id) {
        couponService.enableCoupon(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<List<String>> autocomplete(
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(couponService.autocomplete(keyword));
    }

}