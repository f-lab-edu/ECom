package com.example.api.module.cart.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.module.cart.controller.request.CartProductAddRequest;
import com.example.api.module.cart.controller.request.CartProductQuantityUpdateRequest;
import com.example.api.module.cart.controller.response.CartSummaryResponse;
import com.example.api.module.cart.service.CartService;
import com.example.core.model.response.DataResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    @PreAuthorize("hasRole('USER')")
    @GetMapping()
    public DataResponse<CartSummaryResponse> getCart(
            @AuthenticationPrincipal Long userId) {
        return DataResponse.of(cartService.getCart(userId));
    }


    @PreAuthorize("hasRole('USER')")
    @PostMapping("/products")
    public DataResponse<CartSummaryResponse> addCartProduct(
            @AuthenticationPrincipal Long userId,
            @RequestBody CartProductAddRequest req) {
        return DataResponse.of(cartService.addCartProduct(userId, req));
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/products/{productId}")
    public DataResponse<CartSummaryResponse> updateCartProduct(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long productId,
            @RequestBody CartProductQuantityUpdateRequest req) {
        return DataResponse.of(cartService.updateCartProductQuantity(userId, productId, req));
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/products/{productId}")
    public DataResponse<CartSummaryResponse> deleteCartProduct(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long productId) {
        return DataResponse.of(cartService.deleteCartProduct(userId, productId));
    }
}
