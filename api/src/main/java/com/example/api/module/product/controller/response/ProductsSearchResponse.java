package com.example.api.module.product.controller.response;

import com.example.core.dto.ProductSearchDto;
import lombok.*;

@Setter
@Getter
@Builder
@ToString
@AllArgsConstructor
public class ProductsSearchResponse {
    private Long id;
    private String productName;
    private Long price;
    private Long stockQuantity;
    private String thumbnailUrl;
    private String categoryName;
}
