package com.example.core.dto;

import com.example.core.domain.cart_product.CartProduct;
import com.example.core.enums.PaymentMethod;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CartProductPaymentRequestDto {
    private Long userId;
    private Long cartId;
    private Long productId;
    private Long quantity;
    private PaymentMethod paymentMethod;

    public static CartProductPaymentRequestDto of(Long userId, CartProduct cartProduct, PaymentMethod paymentMethod) {
        return CartProductPaymentRequestDto.builder()
                .userId(userId)
                .cartId(cartProduct.getCart().getId())
                .productId(cartProduct.getProduct().getId())
                .quantity(cartProduct.getQuantity())
                .paymentMethod(paymentMethod)
                .build();
    }
}
