package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.requests.CartRequest;
import com.ecommerce.backend.dto.responses.CartResponse;
import com.ecommerce.backend.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carts")
@CrossOrigin
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // lấy tất cả cart
    @GetMapping
    public List<CartResponse> getAllCarts() {
        return cartService.getAllCarts();
    }

    // lấy cart theo id
    @GetMapping("/{id}")
    public CartResponse getCartById(@PathVariable Integer id) {
        return cartService.getCartById(id);
    }

    // tạo cart
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CartResponse createCart(@Valid @RequestBody CartRequest request) {
        return cartService.createCart(request);
    }

    // xoá cart
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCart(@PathVariable Integer id) {
        cartService.deleteCart(id);
    }

    // lấy cart theo user
    @GetMapping("/user/{userId}")
    public CartResponse getCartByUser(@PathVariable Integer userId) {
        return cartService.getCartByUser(userId);
    }
}