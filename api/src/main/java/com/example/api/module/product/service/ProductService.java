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
import com.example.core.dto.ProductImageDto;
import com.example.core.dto.ProductSearchConditionDto;
import com.example.core.dto.ProductSearchDto;
import com.example.core.exception.BadRequestException;
import com.example.core.utils.MinioUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
        Pageable pageable = PageRequest.of(condition.getPage(), condition.getSize());
        ProductSearchConditionDto conditionDto = ProductSearchConditionRequest.toDto(condition);
        List<ProductSearchDto> productSearchDtoList = productApiRepository.findProductsByCondition(conditionDto);

        List<Long> productIds = productSearchDtoList.stream()
                .map(ProductSearchDto::getId)
                .toList();

        Map<Long, String> thumbnailMap = productImageApiRepository.findThumbnailsByProductIds(productIds);

        List<ProductsSearchResponse> responseList = productSearchDtoList.stream()
                .map(dto -> new ProductsSearchResponse(
                        dto.getId(),
                        dto.getProductName(),
                        dto.getPrice(),
                        dto.getStockQuantity(),
                        thumbnailMap.get(dto.getId()),
                        dto.getCategoryName()
                ))
                .toList();

        long totalCount = productApiRepository.countProductsByCondition(conditionDto);

        return new PageImpl<>(responseList, pageable, totalCount);
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
            return ImageUploadResponse.from(minioUtil.upload(file, "products"));

        } catch (Exception e) {
            log.error("Image upload failed", e);
            throw new RuntimeException("Image upload failed");
        }
    }

    @Transactional
    public Long createProduct(ProductCreateRequest request) {
        Category category = categoryApiRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BadRequestException("Category not found"));

        List<ProductImageDto> images = request.getProductImages();
        String thumbnailUrl = images.get(0).getUrl();

        // create product
        Product product = Product.of(
                request.getName(),
                request.getDescription(),
                request.getStockQuantity(),
                request.getPrice(),
                thumbnailUrl,
                category
        );
        productApiRepository.save(product);

        // create product images

        List<ProductImage> productImages = ProductImage.from(images, product);
        productImageApiRepository.saveAll(productImages);

        product.setProductImages(productImages);
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
