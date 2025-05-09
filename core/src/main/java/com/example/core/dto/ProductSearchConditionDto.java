package com.example.core.dto;

import lombok.Data;

@Data
public class ProductSearchConditionDto {

    private int page = 0;
    private int size = 10;

    private int minPrice = 0;
    private int maxPrice = Integer.MAX_VALUE;

    private String sort = "price, asc";

    private Long categoryId = -1L;

    public ProductSearchConditionDto of(int page, int size, int minPrice, int maxPrice, String sort, Long categoryId) {
        this.page = page;
        this.size = size;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.sort = sort;
        this.categoryId = categoryId;
        return this;
    }
}
