package com.example.core.domain.product_image.api;

import com.example.core.domain.product_image.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Map;

public interface ProductImageApiRepositoryCustom {

    Map<Long, String> findThumbnailsByProductIds(List<Long> productIds);
}
