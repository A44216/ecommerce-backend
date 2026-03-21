package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.requests.CartItemRequest;
import com.ecommerce.backend.dto.responses.CartItemResponse;
import com.ecommerce.backend.service.CartItemService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart-items")
@CrossOrigin
public class CartItemController {
    private final CartItemService cartItemService;

    public CartItemController(CartItemService cartItemService) {
        this.cartItemService = cartItemService;
    }

    // lấy tất cả item trong cart
    @GetMapping("/cart/{cartId}")
    public List<CartItemResponse> getItemsByCart(@PathVariable Integer cartId) {
        return cartItemService.getItemsByCart(cartId);
    }

    // thêm vào cart
    @PostMapping
    public CartItemResponse addToCart(@Valid @RequestBody CartItemRequest request) {
        return cartItemService.addToCart(request);
    }

    // cập nhật số lượng
    @PutMapping("/{id}")
    public CartItemResponse updateQuantity(@PathVariable Integer id,
                                           @RequestParam int quantity) {
        return cartItemService.updateQuantity(id, quantity);
    }

    // xoá item
    @DeleteMapping("/{id}")
    public void deleteCartItem(@PathVariable Integer id) {
        cartItemService.deleteCartItem(id);
    }
}