package com.example.api.module.order.service;

import com.example.api.module.coupon.service.CouponService;
import com.example.api.module.order.controller.request.OrderProductRequest;
import com.example.api.module.stock.service.StockService;
import com.example.core.domain.order.Order;
import com.example.core.domain.order.api.OrderApiRepository;
import com.example.core.domain.order_product.OrderProduct;
import com.example.core.domain.order_product.api.OrderProductApiRepository;
import com.example.core.domain.product.Product;
import com.example.core.domain.product.api.ProductApiRepository;
import com.example.core.domain.shipping_address.ShippingAddress;
import com.example.core.domain.shipping_address.api.ShippingAddressApiRepository;
import com.example.core.domain.user.User;
import com.example.core.domain.user.api.UserApiRepository;
import com.example.core.dto.OrderProductRequestDto;
import com.example.core.enums.OrderStatus;
import com.example.core.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderTransactionService { // 프록시 문제로 내부 메서드 호출 시 @Transactional이 적용되지 않으므로 Transaction 서비스를 별도로 생성
    private final StockService stockService;
    private final CouponService couponService;

    private final UserApiRepository userApiRepository;
    private final ShippingAddressApiRepository shippingAddressApiRepository;
    private final OrderApiRepository orderApiRepository;
    private final ProductApiRepository productApiRepository;
    private final OrderProductApiRepository orderProductApiRepository;

    @Transactional
    public Order createOrder(Long userId, OrderProductRequest req) {
        User user = userApiRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("user not found"));
        ShippingAddress shippingAddress = shippingAddressApiRepository.findById(req.getShippingAddressId())
                .orElseThrow(() -> new BadRequestException("shipping address not found"));

        Order order = Order.of(user, shippingAddress);

        for (OrderProductRequestDto dto : req.getOrderProductDtos()) {
            Product product = productApiRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new BadRequestException("product not found"));

            OrderProduct orderProduct = OrderProduct.createOrderProduct(order, product, dto.getQuantity(), product.getPrice());
            order.addOrderProduct(orderProduct);
        }
        orderApiRepository.save(order);
        orderProductApiRepository.saveAll(order.getOrderProducts());
        return order;
    }

    @Transactional
    public void finalizeOrderSuccess(Long orderId, OrderProductRequest req) {
        Order order = orderApiRepository.findById(orderId)
                .orElseThrow(() -> new BadRequestException("order not found"));
        // 재고 확정
        stockService.confirmStock(req.getOrderProductDtos());
        // 쿠폰 확정
        if (req.getCouponId() != null) couponService.confirmCoupon(req.getCouponId());
        // 주문 상태 확정
        order.setStatus(OrderStatus.PAID);
    }
}
