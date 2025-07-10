package com.example.api.module.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
import com.example.core.domain.product_image.api.ProductImageApiRepository;
import com.example.core.dto.ProductImageDto;
import com.example.core.dto.ProductSearchConditionDto;
import com.example.core.dto.ProductSearchDto;
import com.example.core.exception.BadRequestException;
import com.example.core.utils.MinioUtil;

@SpringBootTest
@ActiveProfiles({"api-test", "core-test"})
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @MockBean
    private ProductApiRepository productApiRepository;

    @MockBean
    private CategoryApiRepository categoryApiRepository;

    @MockBean
    private ProductImageApiRepository productImageApiRepository;

    @MockBean
    private MinioUtil minioUtil;

    private Category testCategory;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .name("Test Category")
                .build();

        testProduct = Product.of("Test Product", "description", 100L, 10000L, "thumbnail", testCategory);
        testProduct.setId(1L);

        // Common mocking for repository calls
        when(productApiRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product argument = invocation.getArgument(0);
            if (argument.getId() == null) {
                argument.setId(2L); // Simulate saving new product with a new ID
            }
            return argument;
        });
    }

    // =========================== getProducts 테스트 (중요도: 높음) ===========================

    @Test
    void getProducts_성공_기본조건() {
        // given
        ProductSearchConditionRequest condition = new ProductSearchConditionRequest();
        condition.setPage(0);
        condition.setSize(10);
        ProductSearchDto productSearchDto = new ProductSearchDto(
                testProduct.getId(),
                testProduct.getProductName(),
                testProduct.getPrice(),
                testProduct.getStockQuantity(),
                testCategory.getName()
        );
        List<ProductSearchDto> productSearchDtoList = List.of(productSearchDto);

        when(productApiRepository.findProductsByCondition(any(ProductSearchConditionDto.class))).thenReturn(productSearchDtoList);
        when(productApiRepository.countProductsByCondition(any(ProductSearchConditionDto.class))).thenReturn(1L);
        when(productImageApiRepository.findThumbnailsByProductIds(any())).thenReturn(Map.of(testProduct.getId(), "thumbnail_url"));

        // when
        Page<ProductsSearchResponse> result = productService.getProducts(condition);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testProduct.getId(), result.getContent().get(0).getId());
    }

    @Test
    void getProducts_성공_특정카테고리검색() {
        // given
        ProductSearchConditionRequest condition = new ProductSearchConditionRequest();
        condition.setPage(0);
        condition.setSize(10);
        condition.setCategoryId(testCategory.getId());

        ProductSearchDto productSearchDto = new ProductSearchDto(testProduct.getId(), "Filtered Product", 10000L, 100L, testCategory.getName());
        when(productApiRepository.findProductsByCondition(any(ProductSearchConditionDto.class))).thenReturn(List.of(productSearchDto));
        when(productApiRepository.countProductsByCondition(any(ProductSearchConditionDto.class))).thenReturn(1L);
        when(productImageApiRepository.findThumbnailsByProductIds(any())).thenReturn(Map.of(testProduct.getId(), "thumbnail_url"));

        // when
        Page<ProductsSearchResponse> result = productService.getProducts(condition);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Category", result.getContent().get(0).getCategoryName());
    }

    @Test
    void getProducts_성공_빈결과() {
        // given
        ProductSearchConditionRequest condition = new ProductSearchConditionRequest();
        condition.setPage(0);
        condition.setSize(10);
        condition.setCategoryId(99999L); // 존재하지 않는 카테고리로 빈 결과 테스트

        when(productApiRepository.findProductsByCondition(any(ProductSearchConditionDto.class))).thenReturn(Collections.emptyList());
        when(productApiRepository.countProductsByCondition(any(ProductSearchConditionDto.class))).thenReturn(0L);

        // when
        Page<ProductsSearchResponse> result = productService.getProducts(condition);

        // then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    // =========================== getProduct 테스트 (중요도: 높음) ===========================

    @Test
    void getProduct_성공() {
        // given
        Long productId = testProduct.getId();
        when(productApiRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(productImageApiRepository.findByProductId(productId)).thenReturn(List.of());

        // when
        ProductResponse result = productService.getProduct(productId);

        // then
        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals(testProduct.getProductName(), result.getName());
    }

    @Test
    void getProduct_실패_존재하지않는상품() {
        // given
        Long nonExistentId = 99999L;
        when(productApiRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> productService.getProduct(nonExistentId));
        assertEquals("Product not found", exception.getMessage());
    }

    // =========================== uploadImage 테스트 (중요도: 보통) ===========================

    @Test
    void uploadImage_성공() throws IOException {
        // given
        MockMultipartFile file = new MockMultipartFile(
            "image", "test.jpg", "image/jpeg", "test image content".getBytes()
        );

        when(minioUtil.upload(any(MultipartFile.class), eq("products")))
            .thenReturn(new com.example.core.dto.ImageUploadDto("test-uuid", "http://test-url.com"));

        // when
        ImageUploadResponse result = productService.uploadImage(file);

        // then
        assertNotNull(result);
        assertEquals("test-uuid", result.getImageId());
        assertEquals("http://test-url.com", result.getUrl());
    }

    @Test
    void uploadImage_실패_IOException() throws IOException {
        // given
        MockMultipartFile file = new MockMultipartFile(
            "image", "test.jpg", "image/jpeg", "test image content".getBytes()
        );

        when(minioUtil.upload(any(MultipartFile.class), eq("products")))
            .thenThrow(new IOException("Upload failed"));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> productService.uploadImage(file));
        assertEquals("Image upload failed", exception.getMessage());
    }

    // =========================== createProduct 테스트 (중요도: 높음) ===========================

    @Test
    void createProduct_성공() {
        // given
        ProductCreateRequest request = new ProductCreateRequest();
        request.setName("New Product");
        request.setDescription("New Description");
        request.setStockQuantity(50L);
        request.setPrice(20000L);
        request.setCategoryId(testCategory.getId());

        ProductImageDto imageDto = new ProductImageDto();
        imageDto.setImageId("image-uuid");
        imageDto.setUrl("http://test-image.com");
        request.setProductImages(List.of(imageDto));

        when(categoryApiRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));

        // when
        Long productId = productService.createProduct(request);

        // then
        assertNotNull(productId);
        verify(productApiRepository, times(1)).save(any(Product.class));
        verify(productImageApiRepository, times(1)).saveAll(anyList());
    }

    @Test
    void createProduct_실패_존재하지않는카테고리() {
        // given
        ProductCreateRequest request = new ProductCreateRequest();
        request.setName("New Product");
        request.setDescription("New Description");
        request.setStockQuantity(50L);
        request.setPrice(20000L);
        request.setCategoryId(99999L); // 존재하지 않는 카테고리

        when(categoryApiRepository.findById(99999L)).thenReturn(Optional.empty());

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> productService.createProduct(request));
        assertEquals("Category not found", exception.getMessage());
    }

    @Test
    void createProduct_성공_상품이미지생성확인() {
        // given
        ProductCreateRequest request = new ProductCreateRequest();
        request.setName("New Product");
        request.setDescription("New Description");
        request.setStockQuantity(50L);
        request.setPrice(20000L);
        request.setCategoryId(testCategory.getId());

        ProductImageDto imageDto = new ProductImageDto();
        imageDto.setImageId("image-uuid");
        imageDto.setUrl("http://test-image.com");
        request.setProductImages(List.of(imageDto));

        when(categoryApiRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));

        // when
        Long productId = productService.createProduct(request);

        // then
        assertNotNull(productId);
        verify(productImageApiRepository).saveAll(anyList());
    }

    // =========================== updateProduct 테스트 (중요도: 높음) ===========================

    @Test
    void updateProduct_성공_전체필드수정() {
        // given
        Long productId = testProduct.getId();
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setName("Updated Product");
        request.setDescription("Updated Description");
        request.setStockQuantity(150L);
        request.setPrice(25000L);
        request.setCategoryId(2L);

        Category newCategory = Category.builder().id(2L).name("new-category").build();
        when(categoryApiRepository.findById(2L)).thenReturn(Optional.of(newCategory));
        when(productApiRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // when
        productService.updateProduct(productId, request);

        // then
        assertEquals("Updated Product", testProduct.getProductName());
        assertEquals("Updated Description", testProduct.getDescription());
        assertEquals(150L, testProduct.getStockQuantity());
        assertEquals(25000L, testProduct.getPrice());
        assertEquals(newCategory.getId(), testProduct.getCategory().getId());
    }

    @Test
    void updateProduct_성공_부분필드수정() {
        // given
        Long productId = testProduct.getId();
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setName("Partially Updated Product");

        when(productApiRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // when
        productService.updateProduct(productId, request);

        // then
        assertEquals("Partially Updated Product", testProduct.getProductName());
        // 다른 필드들은 그대로인지 확인
        assertEquals("description", testProduct.getDescription());
    }

    @Test
    void updateProduct_실패_존재하지않는상품() {
        // given
        Long nonExistentId = 99999L;
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setName("This should fail");

        when(productApiRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> productService.updateProduct(nonExistentId, request));
        assertEquals("Product not found", exception.getMessage());
    }

    // =========================== deleteProduct 테스트 (중요도: 높음) ===========================

    @Test
    void deleteProduct_성공() {
        // given
        Long productId = testProduct.getId();
        when(productApiRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // when
        productService.deleteProduct(productId);

        // then
        // No exception thrown means success. We can also verify mock interactions.
        verify(productApiRepository, times(1)).findById(productId);
    }

    @Test
    void deleteProduct_실패_존재하지않는상품() {
        // given
        Long nonExistentId = 99999L;
        when(productApiRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> productService.deleteProduct(nonExistentId));
        assertEquals("Product not found", exception.getMessage());
    }
} 