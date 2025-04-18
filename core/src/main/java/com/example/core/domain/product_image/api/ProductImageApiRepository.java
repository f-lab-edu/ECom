package com.example.core.domain.product_image.api;

import com.example.core.domain.product_image.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageApiRepository extends JpaRepository<ProductImage, String> {

}
