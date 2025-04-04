package com.example.core.domain.user;

import com.example.core.domain.BaseEntity;
import com.example.core.domain.cart.Cart;
import com.example.core.domain.shipping_address.ShippingAddress;
import com.example.core.domain.user.meta.Status;
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
@NoArgsConstructor
@AllArgsConstructor
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
    private List<ShippingAddress> shippingAddressList = new ArrayList<>();

    @OneToOne(mappedBy = "user")
    private Cart cart;


    public static User of(String email,
                          String nickname,
                          String salt,
                          String hashedPassword,
                          String phoneNumber) {
        return User.builder()
                   .email(email)
                   .nickname(nickname)
                   .salt(salt)
                   .hashedPassword(hashedPassword)
                   .phoneNumber(phoneNumber)
                   .status(Status.ACTIVE)
                   .build();
    }
}
