package com.example.api.module.product.controller.request;

import com.example.core.dto.ProductSearchConditionDto;
import lombok.Data;

@Data
public class ProductSearchConditionRequest {

    private int page = 0;
    private int size = 10;

    private int minPrice = 0;
    private int maxPrice = Integer.MAX_VALUE;

    private String sort = "price, asc";

    private Long categoryId = 0L;


    public static ProductSearchConditionDto toDto(ProductSearchConditionRequest condition) {
        return new ProductSearchConditionDto()
                .of(condition.getPage(), condition.getSize(), condition.getMinPrice(), condition.getMaxPrice(), condition.getSort(), condition.getCategoryId());
    }
}
