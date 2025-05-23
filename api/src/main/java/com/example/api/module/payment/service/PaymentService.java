package com.example.api.module.payment.service;

import com.example.api.module.payment.controller.request.PaymentRequest;
import com.example.api.module.payment.controller.response.PaymentResponse;
import com.example.api.module.payment.externalPayment.service.ExternalPaymentService;
import com.example.core.domain.order.Order;
import com.example.core.domain.order.api.OrderApiRepository;
import com.example.core.domain.order_product.OrderProduct;
import com.example.core.domain.payment.Payment;
import com.example.core.domain.payment.api.PaymentApiRepository;
import com.example.core.domain.product.Product;
import com.example.core.domain.shipping.Shipping;
import com.example.core.domain.shipping.api.ShippingApiRepository;
import com.example.core.dto.OrderDto;
import com.example.core.enums.OrderStatus;
import com.example.core.enums.PaymentMehtod;
import com.example.core.enums.PaymentStatus;
import com.example.core.exception.BadRequestException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final ExternalPaymentService externalPaymentService;

    private final OrderApiRepository orderApiRepository;
    private final PaymentApiRepository paymentApiRepository;
    private final ShippingApiRepository shippingApiRepository;

    @Transactional
    public PaymentResponse makePayment(Long userId, PaymentRequest paymentRequest) {
        Long orderId = paymentRequest.getOrderId();
        PaymentMehtod paymentMehtod = paymentRequest.getPaymentMethod();
        Long paymentAmount = paymentRequest.getPaymentAmount();

        Order orderOpt = orderApiRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new BadRequestException("Order not found or does not belong to user"));
        
        if (!orderOpt.getStatus().equals(OrderStatus.CREATED)) {
            throw new BadRequestException("Order " + orderId + " has status " + orderOpt.getStatus());
        }

        Payment payment = Payment.createPayment(paymentMehtod, paymentAmount, null);

        List<OrderProduct> orderProducts = orderOpt.getOrderProducts();


        List<Shipping> shippingList = new ArrayList<>();
        for (OrderProduct orderProduct : orderProducts) {
            shippingList.add(Shipping.of(orderProduct));
        }

        if (externalPaymentService.sendPaymentRequestIsSuccess(paymentMehtod, paymentAmount)) {
            orderOpt.setStatus(OrderStatus.PAID);
            orderApiRepository.save(orderOpt);

            payment.setPaymentStatus(PaymentStatus.COMPLETED);

            shippingApiRepository.saveAll(shippingList);
        } else { // 결제 실패 시
            orderOpt.setStatus(OrderStatus.CANCELLED);
            orderApiRepository.save(orderOpt);

            payment.setPaymentStatus(PaymentStatus.FAILED);

            // 재고 수량 복구
            for (OrderProduct orderProduct : orderProducts) {
                Product product = orderProduct.getProduct();
                product.setStockQuantity(product.getStockQuantity() + orderProduct.getQuantity());
            }
        }

        orderOpt.setPayment(payment);
        paymentApiRepository.save(payment);
        return PaymentResponse.of(payment, OrderDto.from(orderOpt));
    }
}
