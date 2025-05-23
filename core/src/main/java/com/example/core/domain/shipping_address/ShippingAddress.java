package com.example.core.domain.shipping_address;

import com.example.core.domain.order.Order;
import com.example.core.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

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
public class ShippingAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String recipientName;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, length = 20)
    private String zipCode;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Column(nullable = false)
    private Boolean isDefault = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "shippingAddress")
    private List<Order> orders;


    public static ShippingAddress of(String recipientName,
                                      String address,
                                      String zipCode,
                                      String phoneNumber,
                                      Boolean isDefault) {
        return ShippingAddress.builder()
                .recipientName(recipientName)
                .address(address)
                .zipCode(zipCode)
                .phoneNumber(phoneNumber)
                .isDefault(isDefault)
                .build();
    }
}
