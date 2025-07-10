package com.example.core.domain.product_image.api;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.core.domain.product_image.ProductImage;

public interface ProductImageApiRepository extends JpaRepository<ProductImage, String>, ProductImageApiRepositoryCustom {

    List<ProductImage> findByProductId(Long productId);
}
