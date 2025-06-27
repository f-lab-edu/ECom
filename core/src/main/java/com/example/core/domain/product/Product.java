package com.example.core.domain.product;

import com.example.core.domain.BaseEntity;
import com.example.core.domain.category.Category;
import com.example.core.domain.order_product.OrderProduct;
import com.example.core.domain.product_image.ProductImage;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Entity
@Table
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String productName;

    @Column
    private String description;

    @Column
    private Long stockQuantity;

    @Column
    private Long price;

    @OneToMany(mappedBy = "product")
    @Builder.Default
    private List<ProductImage> productImages = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    @Builder.Default
    private List<OrderProduct> orderProducts = new ArrayList<>();

    @Column
    private String thumbnailUrl;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column
    private boolean isDeleted;

    @Column
    private LocalDateTime deletedAt;

    public static Product of(String productName, String description, Long stockQuantity, Long price, String thumbnailUrl, Category category) {
        return Product.builder()
                .productName(productName)
                .description(description)
                .stockQuantity(stockQuantity)
                .price(price)
                .thumbnailUrl(thumbnailUrl)
                .category(category)
                .isDeleted(false)
                .build();
    }

    public void decreaseStock(Long quantity) {
        stockQuantity -= quantity;
    }
}
