package com.example.api.module.order.service;

import com.example.api.module.coupon.service.CouponService;
import com.example.api.module.order.controller.request.OrderProductRequest;
import com.example.api.module.order.controller.response.OrderProductResponse;
import com.example.api.module.payment.service.PaymentService;
import com.example.api.module.stock.service.StockService;
import com.example.core.domain.order.Order;
import com.example.core.domain.order.api.OrderApiRepository;
import com.example.core.domain.order_product.OrderProduct;
import com.example.core.domain.shipping_address.api.ShippingAddressApiRepository;
import com.example.core.domain.user.User;
import com.example.core.domain.user.api.UserApiRepository;
import com.example.core.dto.OrderProductRequestDto;
import com.example.core.enums.OrderStatus;
import com.example.core.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class OrderService {

    private final StockService stockService;
    private final PaymentService paymentService;
    private final OrderTransactionService orderTransactionService;
    private final CouponService couponService;

    private final ShippingAddressApiRepository shippingAddressApiRepository;
    private final UserApiRepository userApiRepository;
    private final OrderApiRepository orderApiRepository;



    @Transactional(readOnly = true)
    public OrderProductResponse getOrder(Long userId, Long orderId) {
        // 0. 유저 조회
        User user = userApiRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("user not found"));

        // 1. 주문 조회
        Order order = orderApiRepository.findById(orderId)
                .orElseThrow(() -> new BadRequestException("order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("not your order");
        }

        // 2. 주문 상품들 조회
        List<OrderProduct> orderProducts = order.getOrderProducts();

        return OrderProductResponse.of(userId, order, orderProducts);
    }

    @Transactional(readOnly = true)
    public List<OrderProductResponse> getOrders(Long userId) {
        User user = userApiRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("user not found"));

        List<OrderProductResponse> orderProductResponses = new ArrayList<>();
        for (Order order : user.getOrders()) {
            orderProductResponses.add(getOrder(userId, order.getId()));
        }

        return orderProductResponses;
    }

    // Order a single product
    public OrderProductResponse orderProduct(Long userId, OrderProductRequest req) {
        // 주문 생성
        Order order = orderTransactionService.createOrder(userId, req);

        // 주문 상품 생성 및 재고 확인 & 예약 처리
        List<Long> productRollbackList = new ArrayList<>(); // productId, quantity / couponId, -1
        List<Long> couponRollbackList = new ArrayList<>(); // couponId
        try {
            // 상품 재고 예약 처리
            for (OrderProductRequestDto dto : req.getOrderProductDtos()) {
                long productId = dto.getProductId(), productQuantity = dto.getQuantity();
                boolean reserved = stockService.tryReserve(order.getId(), productId, productQuantity);
                if (!reserved) {
                    throw new BadRequestException("stock not enough for product: " + productId);
                }
                productRollbackList.add(productId);
            }
            log.info("successfully reserved stock for order: {}", order.getId());


            // 쿠폰 적용 확인 & 예약 처리
            Long couponId = req.getCouponId();
            if (couponId != null) {
                boolean couponReserved = couponService.tryReserve(userId, order.getId(), req.getCouponId());
                if (!couponReserved) {
                    throw new BadRequestException("coupon not available or already used");
                }
                couponRollbackList.add(couponId);
            }
            log.info("successfully reserved coupon for order: {}", order.getId());

            // 결제 시도
            String transactionId = paymentService.processPayment(userId, order.getId(), req.getPaymentRequestDto());
            log.info("successfully payment for order: {}", order.getId());

            // 결제 후  확정 단계, 쿠폰 확정
            orderTransactionService.finalizeOrderSuccess(order.getId(), req);
            return OrderProductResponse.of(userId, order, order.getOrderProducts());

        } catch (Exception e) {
            log.error("CRITICAL ORDER FAILURE - OrderId: {}. Full stack trace:", order.getId(), e);
            order.setStatus(OrderStatus.CANCELLED);
            // 재고 부족으로 주문 실패 시, 재고, 쿠폰 복구 (내부적으로 3회 재시도)
            rollbackStock(order.getId(), productRollbackList);
            rollbackCoupon(order.getId(), couponRollbackList);
            throw new BadRequestException("order failed: " + e.getMessage());
        }
    }

    /**
     * 재고 롤백 메소드
     * @param rollbackList 롤백할 목록 (productId, quantity / couponId, -1)
     */
    private void rollbackStock(Long orderId, List<Long> rollbackList) {
        for (Long productId : rollbackList) {
            stockService.revertReservation(orderId, productId);
        }
    }
    private void rollbackCoupon(Long orderId, List<Long> couponList) {
        for (Long couponId : couponList) {
            couponService.revertReservation(orderId, couponId);
        }
    }
}
