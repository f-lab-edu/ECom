package com.example.api.module.product.controller.response;

import com.example.core.dto.ProductSearchDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Builder
@ToString
public class ProductSearchResponse {
    private Long id;
    private String productName;
    private Long price;
    private String thumbnailUrl;
    private String categoryName;

    public static ProductSearchResponse from(ProductSearchDto productSearchDto) {
        return ProductSearchResponse.builder()
                .id(productSearchDto.getId())
                .productName(productSearchDto.getProductName())
                .price(productSearchDto.getPrice())
                .thumbnailUrl(productSearchDto.getThumbnailUrl())
                .categoryName(productSearchDto.getCategoryName())
                .build();
    }
}
