package com.example.api.module.product.controller.request;

import com.example.core.dto.ProductImageDto;
import lombok.Data;

import java.util.List;

@Data
public class ProductCreateRequest {
    private String name;
    private String description;
    private Long price;
    private Long stockQuantity;
    private Long categoryId;
    private List<ProductImageDto> productImages;
}
