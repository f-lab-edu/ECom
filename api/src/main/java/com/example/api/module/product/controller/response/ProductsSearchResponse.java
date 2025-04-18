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
public class ProductsSearchResponse {
    private Long id;
    private String productName;
    private Long price;
    private String thumbnailUrl;
    private String categoryName;

    public static ProductsSearchResponse from(ProductSearchDto productSearchDto) {
        return ProductsSearchResponse.builder()
                .id(productSearchDto.getId())
                .productName(productSearchDto.getProductName())
                .price(productSearchDto.getPrice())
                .thumbnailUrl(productSearchDto.getThumbnailUrl())
                .categoryName(productSearchDto.getCategoryName())
                .build();
    }
}
