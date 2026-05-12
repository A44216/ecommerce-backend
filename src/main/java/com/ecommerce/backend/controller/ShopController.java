package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.requests.ShopRequest;
import com.ecommerce.backend.dto.responses.ShopResponse;
import com.ecommerce.backend.service.ShopService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shops")
@CrossOrigin
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    // tất cả shop
    @GetMapping
    public List<ShopResponse> getAllShops() {
        return shopService.getAllShops();
    }

    // shop theo id
    @GetMapping("/{id}")
    public ShopResponse getShopById(@PathVariable Integer id) {
        return shopService.getShopById(id);
    }

    // shop theo user
    @GetMapping("/user/{userId}")
    public ShopResponse getShopByUser(@PathVariable Integer userId) {
        return shopService.getShopByUser(userId);
    }

    // tạo shop
    @PostMapping
    public ShopResponse createShop(@RequestBody ShopRequest request) {
        return shopService.createShop(request);
    }

    // update
    @PutMapping("/{id}")
    public ShopResponse updateShop(@PathVariable Integer id,
                                   @RequestBody ShopRequest request) {
        return shopService.updateShop(id, request);
    }

    // delete
    @DeleteMapping("/{id}")
    public void deleteShop(@PathVariable Integer id) {
        shopService.deleteShop(id);
    }

    @PutMapping("/{id}/ai-reply")
    public ShopResponse toggleAiReply(@PathVariable Integer id, @RequestParam Boolean enabled) {
        return shopService.toggleAiReply(id, enabled);
    }
}