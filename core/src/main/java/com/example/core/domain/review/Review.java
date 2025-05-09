package com.example.core.domain.review;

import com.example.core.domain.BaseEntity;
import com.example.core.domain.product.Product;
import com.example.core.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Table
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column
    private String title;

    @Column
    private String content;

    @Column(nullable = false)
    private Integer rating;
}
