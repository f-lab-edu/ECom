package com.example.core.domain.cart;

import com.example.core.domain.BaseEntity;
import com.example.core.domain.cart_product.CartProduct;
import com.example.core.domain.product.Product;
import com.example.core.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Entity
@Table
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Cart extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "cart", fetch = FetchType.LAZY)
    private User user;

    @OneToMany(mappedBy = "cart")
    private final List<CartProduct> cartProducts = new ArrayList<>();

    public static Cart of(User user) {
        return builder()
                .user(user)
                .build();
    }
}
