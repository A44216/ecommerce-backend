package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.CartItemRequest;
import com.ecommerce.backend.dto.responses.CartItemResponse;
import com.ecommerce.backend.entity.Cart;
import com.ecommerce.backend.entity.CartItem;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.exception.BadRequestException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.CartItemRepository;
import com.ecommerce.backend.repository.CartRepository;
import com.ecommerce.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartItemService(CartItemRepository cartItemRepository,
                           CartRepository cartRepository,
                           ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    private CartItemResponse mapToDTO(CartItem item) {

        Product product = item.getProduct();

        String image = null;

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            image = product.getImages()
                    .getFirst()
                    .getImageUrl();
        }

        return new CartItemResponse(
                item.getId(),
                product.getId(),
                product.getName(),
                image,
                product.getPrice(),
                item.getQuantity()
        );
    }

    private Cart getCartOrThrow(Integer id) {
        return cartRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart not found with id: " + id));
    }

    private Product getProductOrThrow(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found with id: " + id));
    }

    private CartItem getCartItemOrThrow(Integer id) {
        return cartItemRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("CartItem not found with id: " + id));
    }

    private CartItem createCartItem(Cart cart, Product product, int quantity) {
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(quantity);
        return item;
    }

    public List<CartItemResponse> getItemsByCart(Integer cartId) {
        return cartItemRepository.findByCartId(cartId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional
    public CartItemResponse addToCart(CartItemRequest request) {

        Cart cart = getCartOrThrow(request.getCartId());
        Product product = getProductOrThrow(request.getProductId());

        if (product.getStock() < request.getQuantity()) {
            throw new BadRequestException("Product out of stock");
        }

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(request.getCartId(), request.getProductId())
                .orElse(null);

        if (cartItem != null) {

            int newQuantity = cartItem.getQuantity() + request.getQuantity();

            if (newQuantity > product.getStock()) {
                throw new BadRequestException("Quantity exceeds stock");
            }

            cartItem.setQuantity(newQuantity);

        } else {
            cartItem = createCartItem(cart, product, request.getQuantity());
        }

        return mapToDTO(cartItemRepository.save(cartItem));
    }

    @Transactional
    public CartItemResponse updateQuantity(Integer id, int quantity) {

        CartItem item = getCartItemOrThrow(id);

        if (quantity > item.getProduct().getStock()) {
            throw new BadRequestException("Quantity exceeds stock");
        }

        item.setQuantity(quantity);

        return mapToDTO(cartItemRepository.save(item));
    }

    @Transactional
    public void deleteCartItem(Integer id) {

        CartItem item = getCartItemOrThrow(id);

        cartItemRepository.delete(item);
    }
}