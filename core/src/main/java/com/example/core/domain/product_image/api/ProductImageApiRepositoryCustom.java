package com.example.core.domain.product_image.api;

import java.util.List;
import java.util.Map;

public interface ProductImageApiRepositoryCustom {

    Map<Long, String> findThumbnailsByProductIds(List<Long> productIds);
}
