package com.example.api.module.product.controller;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.api.module.product.controller.request.ProductCreateRequest;
import com.example.api.module.product.controller.request.ProductSearchConditionRequest;
import com.example.api.module.product.controller.request.ProductUpdateRequest;
import com.example.api.module.product.controller.response.ImageUploadResponse;
import com.example.api.module.product.controller.response.ProductResponse;
import com.example.api.module.product.controller.response.ProductsSearchResponse;
import com.example.api.module.product.service.ProductService;
import com.example.core.model.response.DataResponse;

import lombok.RequiredArgsConstructor;

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
    public DataResponse<ProductResponse> getProduct(@PathVariable("productId") Long productId) {
        return DataResponse.of(productService.getProduct(productId));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @PostMapping("/image")
    public DataResponse<ImageUploadResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        System.out.println("/image controller");
        return DataResponse.of(productService.uploadImage(file));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @PostMapping()
    public DataResponse<Long> createProduct(@RequestBody ProductCreateRequest request) {
        return DataResponse.of(productService.createProduct(request));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @PutMapping("/{productId}")
    public DataResponse<Long> updateProduct(@PathVariable Long productId, @RequestBody ProductUpdateRequest request) {
        return DataResponse.of(productService.updateProduct(productId, request));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @DeleteMapping("/{productId}")
    public DataResponse<Long> deleteProduct(@PathVariable Long productId) {
        return DataResponse.of(productService.deleteProduct(productId));
    }
}
