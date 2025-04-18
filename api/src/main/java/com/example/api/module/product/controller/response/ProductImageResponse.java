package com.example.api.module.product.controller.response;

import com.example.core.domain.product_image.ProductImage;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Builder
@ToString
public class ProductImageResponse {
    private String id;
    private String imageKey;
    private String imageUrl;

    private Boolean isThumbnail;
    private Integer sortOrder;


    public static ProductImageResponse from(ProductImage productImage) {
        return ProductImageResponse.builder()
                .id(productImage.getId())
                .imageKey(productImage.getImageKey())
                .imageUrl(productImage.getImageUrl())
                .isThumbnail(productImage.getIsThumbnail())
                .sortOrder(productImage.getSortOrder())
                .build();
    }
}
