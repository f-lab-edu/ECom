package com.example.core.dto;

import com.example.core.domain.product.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSummaryDto {
    private Long id;
    private String productName;
    private Long price;
    private Long quantity;
    private String thumbnailUrl;
    private boolean soldOut;

    public static ProductSummaryDto from(Product product) {
        return builder()
                .id(product.getId())
                .productName(product.getProductName())
                .price(product.getPrice())
                .quantity(product.getStockQuantity())
                .thumbnailUrl(product.getThumbnailUrl())
                .soldOut(product.getStockQuantity() == 0)
                .build();
    }
}
