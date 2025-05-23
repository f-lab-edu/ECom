package com.example.core.domain.shipping;

import com.example.core.domain.BaseEntity;
import com.example.core.domain.order_product.OrderProduct;
import com.example.core.enums.ShippingCompany;
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
@AllArgsConstructor
@NoArgsConstructor
public class Shipping extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderproduct_id", nullable = false)
    private OrderProduct orderProduct;

    @Enumerated(EnumType.STRING)
    @Column
    private ShippingCompany shippingCompanyEnum;

    @Column
    private String trackingNumber;



    public static Shipping of(OrderProduct orderProduct, ShippingCompany shippingCompany, String trackingNumber) {
        return Shipping.builder()
                .orderProduct(orderProduct)
                .shippingCompanyEnum(shippingCompany)
                .trackingNumber(trackingNumber)
                .build();
    }

    public static Shipping of(OrderProduct orderProduct) {
        return Shipping.builder()
                .orderProduct(orderProduct)
                .shippingCompanyEnum(null)
                .trackingNumber("")
                .build();
    }
}
