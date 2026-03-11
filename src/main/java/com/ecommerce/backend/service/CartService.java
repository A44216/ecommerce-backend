package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.responses.CartResponse;
import com.ecommerce.backend.entity.Cart;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.exception.BadRequestException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.CartRepository;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository,
                       UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
    }

    private CartResponse mapToDTO(Cart cart) {
        return new CartResponse(
                cart.getId(),
                cart.getUser().getId(),
                cart.getUser().getUsername(),
                cart.getCreatedAt()
        );
    }

    private Cart getCartOrThrow(Integer id) {
        return cartRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart not found with id: " + id));
    }

    private User getUserOrThrow(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + id));
    }

    public List<CartResponse> getAllCarts() {
        return cartRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public CartResponse getCartById(Integer id) {
        return mapToDTO(getCartOrThrow(id));
    }

    @Transactional
    public CartResponse createCart(CartRequest request) {

        User user = getUserOrThrow(request.getUserId());

        if (cartRepository.existsByUserId(user.getId())) {
            throw new BadRequestException("User already has a cart");
        }

        Cart cart = new Cart();
        cart.setUser(user);

        return mapToDTO(cartRepository.save(cart));
    }

    @Transactional
    public void deleteCart(Integer id) {

        Cart cart = getCartOrThrow(id);

        cartRepository.delete(cart);
    }

    public CartResponse getCartByUser(Integer userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart not found for user id: " + userId));

        return mapToDTO(cart);
    }
}