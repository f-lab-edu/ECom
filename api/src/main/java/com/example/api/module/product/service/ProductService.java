package com.example.api.module.product.service;

import com.example.api.module.product.controller.request.ProductCreateRequest;
import com.example.api.module.product.controller.request.ProductSearchConditionRequest;
import com.example.api.module.product.controller.request.ProductUpdateRequest;
import com.example.api.module.product.controller.response.ImageUploadResponse;
import com.example.api.module.product.controller.response.ProductResponse;
import com.example.api.module.product.controller.response.ProductsSearchResponse;
import com.example.core.domain.category.Category;
import com.example.core.domain.category.api.CategoryApiRepository;
import com.example.core.domain.product.Product;
import com.example.core.domain.product.api.ProductApiRepository;
import com.example.core.domain.product_image.ProductImage;
import com.example.core.domain.product_image.api.ProductImageApiRepository;
import com.example.core.dto.ProductSearchConditionDto;
import com.example.core.exception.BadRequestException;
import com.example.core.utils.MinioUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final MinioUtil minioUtil;

    private final ProductApiRepository productApiRepository;
    private final ProductImageApiRepository productImageApiRepository;
    private final CategoryApiRepository categoryApiRepository;

    @Transactional(readOnly = true)
    public Page<ProductsSearchResponse> getProducts(ProductSearchConditionRequest condition) {
        ProductSearchConditionDto requestConditionDto = ProductSearchConditionRequest.toDto(condition);
        return productApiRepository.search(requestConditionDto)
                .map(ProductsSearchResponse::from);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long productId) {
        Product product = productApiRepository.findById(productId)
                .orElseThrow(() -> new BadRequestException("Product not found"));
        return ProductResponse.from(product);
    }

    @Transactional
    public ImageUploadResponse uploadImage(MultipartFile file) {
        try {
            ImageUploadResponse result = ImageUploadResponse.from(minioUtil.upload(file));
            ProductImage productImage = ProductImage.builder()
                    .id(result.getImageId())
                    .imageUrl(result.getUrl())
                    .imageKey(result.getKey())
                    .build();
            productImageApiRepository.save(productImage);
            return result;

        } catch (Exception e) {
            log.error("Image upload failed", e);
            throw new RuntimeException("Image upload failed");
        }
    }

    @Transactional
    public Long createProduct(ProductCreateRequest request) {
        Category category = categoryApiRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BadRequestException("Category not found"));

        Product product = Product.builder()
                .productName(request.getName())
                .description(request.getDescription())
                .stockQuantity(request.getStockQuantity())
                .price(request.getPrice())
                .category(category)
                .isDeleted(false)
                .build();
        productApiRepository.save(product);

        List<ProductImage> images = productImageApiRepository.findAllById(
                List.of(request.getImageIds()));


        for (int i=0; i<images.size(); i++) {
            ProductImage img = images.get(i);
            img.setProduct(product);
            img.setSortOrder(request.getImageSortOrder()[i]);
            img.setIsThumbnail(request.getIsThumbnail()[i]);
            product.getProductImages().add(img);
        }

        productImageApiRepository.saveAll(images);
        productApiRepository.save(product);
        return product.getId();
    }

    @Transactional
    public Long updateProduct(Long ProductId, ProductUpdateRequest request) {
        Product product = productApiRepository.findById(ProductId)
                .orElseThrow(() -> new BadRequestException("Product not found"));

        if (request.getName() != null) {
            product.setProductName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getStockQuantity() != null) {
            product.setStockQuantity(request.getStockQuantity());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryApiRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new BadRequestException("Category not found"));
            product.setCategory(category);
        }
        return product.getId();
    }

    @Transactional
    public Long deleteProduct(Long productId) {
        Product product = productApiRepository.findById(productId)
                .orElseThrow(() -> new BadRequestException("Product not found"));
        for (ProductImage image : product.getProductImages()) {
            image.setProduct(null);
        }
        product.setDeleted(true);
        product.setDeletedAt(LocalDateTime.now());
        return product.getId();
    }

}
