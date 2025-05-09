package com.example.api.module.cart.controller.response;

import com.example.core.dto.CartProductDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartSummaryResponse {
    List<CartProductDto> cartProductList;
    Long cartTotalPrice; //총 가격

    public static CartSummaryResponse of(List<CartProductDto> cartProductList) {
        Long totalPrice = cartProductList.stream()
                .mapToLong(CartProductDto::getProductTotalPrice)
                .sum();

        return builder()
                .cartProductList(cartProductList)
                .cartTotalPrice(totalPrice)
                .build();
    }
}
