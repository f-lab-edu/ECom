package com.example.core.domain.product.api;

import com.example.core.domain.product.Product;
import com.example.core.dto.ProductSearchConditionDto;
import com.example.core.dto.ProductSearchDto;
import org.springframework.data.domain.Page;

public interface ProductApiRepositoryCustom {

    Page<ProductSearchDto> search(ProductSearchConditionDto cond);
}
