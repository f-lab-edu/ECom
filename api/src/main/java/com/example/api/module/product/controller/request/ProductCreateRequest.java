package com.example.api.module.product.controller.request;

import lombok.Data;

@Data
public class ProductCreateRequest {
    private String name;
    private String description;
    private Long price;
    private Long stockQuantity;
    private Long categoryId;

    private String[] imageIds;
    private String[] imageUrls;
    private String[] imageKeys;
    private Boolean[] isThumbnail;
    private int[] imageSortOrder;


}
