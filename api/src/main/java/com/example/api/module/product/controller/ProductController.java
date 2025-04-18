package com.example.api.module.product.controller;

import com.example.api.module.product.controller.request.ProductCreateRequest;
import com.example.api.module.product.controller.request.ProductUpdateRequest;
import com.example.api.module.product.controller.request.ProductSearchConditionRequest;
import com.example.api.module.product.controller.response.ImageUploadResponse;
import com.example.api.module.product.controller.response.ProductResponse;
import com.example.api.module.product.controller.response.ProductsSearchResponse;
import com.example.api.module.product.service.ProductService;
import com.example.core.model.response.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;


    //GET /api/v1/products?categoryId={categoryId}&minPrice={minprice}&maxPrice={maxprice}&sort={sortType},{sortBy}&page={page}&size={size}
    @GetMapping()
    public DataResponse<Page<ProductsSearchResponse>> getProducts(@ModelAttribute ProductSearchConditionRequest condition) {
        return DataResponse.of(productService.getProducts(condition));
    }

    @GetMapping("/{productId}")
    public DataResponse<ProductResponse> getProduct(@PathVariable Long productId) {
        return DataResponse.of(productService.getProduct(productId));
    }

//    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @PostMapping("/image")
    public DataResponse<ImageUploadResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        System.out.println("/image controller");
        return DataResponse.of(productService.uploadImage(file));
    }

//    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @PostMapping()
    public DataResponse<Long> createProduct(@RequestBody ProductCreateRequest request) {
        return DataResponse.of(productService.createProduct(request));
    }

//    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @PutMapping("/{productId}")
    public DataResponse<Long> updateProduct(@PathVariable Long productId, @RequestBody ProductUpdateRequest request) {
        return DataResponse.of(productService.updateProduct(productId, request));
    }

//    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @DeleteMapping("/{productId}")
    public DataResponse<Long> deleteProduct(@PathVariable Long productId) {
        return DataResponse.of(productService.deleteProduct(productId));
    }
}
