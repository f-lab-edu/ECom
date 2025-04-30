package com.example.core.domain.product_image;

import com.example.core.domain.BaseEntity;
import com.example.core.domain.product.Product;
import com.example.core.dto.ProductImageDto;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Entity
@Table
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage extends BaseEntity {

    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    @Column
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = true)
    private Product product;

    @Column
    private Integer sortOrder;

    @Column
    private Boolean isThumbnail;

    public static List<ProductImage> from(List<ProductImageDto> productImageDtos, Product product) {
        List<ProductImage> productImages = new ArrayList<>();
        for (ProductImageDto dto : productImageDtos) {
            productImages.add(ProductImage.builder()
                    .imageUrl(dto.getUrl())
                    .product(product)
                    .sortOrder(dto.getImageSortOrder())
                    .isThumbnail(dto.isThumbnail())
                    .build());
        }
        return productImages;

    }
}
