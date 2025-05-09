package com.example.core.dto;

import com.example.core.domain.cart_product.CartProduct;
import com.example.core.enums.CartProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartProductDto {
    ProductSummaryDto productSummary;
    Long quantity;
    CartProductStatus status;
    Long productTotalPrice;

    public static CartProductDto of(ProductSummaryDto productSummary, Long quantity, CartProductStatus status) {
        return builder()
                .productSummary(productSummary)
                .quantity(quantity)
                .productTotalPrice(productSummary.getPrice() * quantity)
                .status(status)
                .build();
    }
}
