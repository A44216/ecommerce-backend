package com.ecommerce.backend.controller.seller;

import com.ecommerce.backend.dto.requests.seller.shop.SellerShopRequest;
import com.ecommerce.backend.dto.responses.seller.shop.SellerShopResponse;
import com.ecommerce.backend.service.seller.SellerShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("hasRole('SELLER')")
@RestController
@RequestMapping("/api/seller/shops")
@RequiredArgsConstructor
public class SellerShopController {

    private final SellerShopService sellerShopService;

    @GetMapping("/me")
    public SellerShopResponse getMyShop() {
        return sellerShopService.getMyShop();
    }

    @PostMapping
    public SellerShopResponse createShop(@Valid @RequestBody SellerShopRequest request) {
        return sellerShopService.createShop(request);
    }

    @PutMapping
    public SellerShopResponse updateShop(@Valid @RequestBody SellerShopRequest request) {
        return sellerShopService.updateShop(request);
    }

    @PatchMapping("/avatar")
    public SellerShopResponse updateAvatar(@RequestParam String avatar) {
        return sellerShopService.updateAvatar(avatar);
    }
}