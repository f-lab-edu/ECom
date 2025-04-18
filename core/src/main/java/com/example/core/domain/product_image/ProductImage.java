package com.example.core.domain.product_image;

import com.example.core.domain.BaseEntity;
import com.example.core.domain.product.Product;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

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
    private String imageKey;

    @Column
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = true)
    private Product product;

    @Column
    private Integer sortOrder;

    @Column
    private Boolean isThumbnail;

}
