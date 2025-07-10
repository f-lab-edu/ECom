package com.example.core.domain.user;

import java.util.ArrayList;
import java.util.List;

import com.example.core.domain.BaseEntity;
import com.example.core.domain.cart.Cart;
import com.example.core.domain.order.Order;
import com.example.core.domain.shipping_address.ShippingAddress;
import com.example.core.domain.user.meta.Status;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    @Column
    private String nickname;

    @Column(nullable = false)
    private String salt;

    @Column(nullable = false)
    private String hashedPassword;

    @Column(nullable = false)
    private String phoneNumber;

    @Column
    private Status status;

    @OneToMany(mappedBy = "user")
    private List<ShippingAddress> shippingAddressList;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false, unique = true)
    private Cart cart;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();



    public void addOrder(Order order) {
        orders.add(order);
        order.setUser(this);
    }

    public static User of(String email,
                          String nickname,
                          String salt,
                          String hashedPassword,
                          String phoneNumber,
                          Cart cart) {
        return User.builder()
                .email(email)
                .nickname(nickname)
                .salt(salt)
                .hashedPassword(hashedPassword)
                .phoneNumber(phoneNumber)
                .status(Status.ACTIVE)
                .cart(cart)
                .build();
    }
}
