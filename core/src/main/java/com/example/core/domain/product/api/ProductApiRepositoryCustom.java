package com.example.core.domain.product.api;

import com.example.core.dto.ProductSearchConditionDto;
import com.example.core.dto.ProductSearchDto;

import java.util.List;

public interface ProductApiRepositoryCustom {
    List<ProductSearchDto> findProductsByCondition(ProductSearchConditionDto conditionDto);
    long countProductsByCondition(ProductSearchConditionDto conditionDto);
}
