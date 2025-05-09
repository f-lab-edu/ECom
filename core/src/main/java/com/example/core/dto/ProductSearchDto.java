package com.example.core.dto;

import com.example.core.domain.category.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchDto {
    private Long id;
    private String productName;
    private Long price;
    private Long stockQuantity;
    private String categoryName;
}
