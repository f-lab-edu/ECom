package com.example.api.module.product.controller.response;

import com.example.core.domain.product.Product;
import com.example.core.domain.product_image.ProductImage;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@Builder
@ToString
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private Long price;
    private Long stockQuantity;
    private String categoryName;
    private List<ProductImage> imageUrls;

    public static ProductResponse of(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getProductName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .categoryName(product.getCategory().getName())
                .imageUrls(product.getProductImages())
                .build();
    }
}
