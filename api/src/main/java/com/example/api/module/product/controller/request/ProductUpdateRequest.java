package com.example.api.module.product.controller.request;

import lombok.Data;

@Data
public class ProductUpdateRequest {
    private String name;
    private String description;
    private Long price;
    private Long stockQuantity;
    private Long categoryId;

}
