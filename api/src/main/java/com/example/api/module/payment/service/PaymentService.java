package com.example.api.module.payment.service;

import org.springframework.stereotype.Service;

import com.example.api.module.payment.externalPayment.service.ExternalPaymentService;
import com.example.core.domain.order.Order;
import com.example.core.domain.payment.Payment;
import com.example.core.dto.PaymentRequestDto;
import com.example.core.enums.PaymentStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class PaymentService {

    private final ExternalPaymentService externalPaymentService;
    private final PaymentTransactionService paymentTransactionService;

    public String processPayment(Long userId, Long orderId, PaymentRequestDto paymentRequestDto) {
       Order order = paymentTransactionService.checkOrderForPayment(orderId, userId);

       Payment payment = paymentTransactionService.createPendingPayment(order, paymentRequestDto);
       log.info("Payment created for order: {}", orderId);

       String transactionId = externalPaymentService.sendPaymentRequestIsSuccess(paymentRequestDto);

        if (transactionId != null) {
            paymentTransactionService.finalizePayment(payment.getId(), transactionId, PaymentStatus.COMPLETED);
            log.info("Payment completed successfully for order: {}", orderId);
            return transactionId;
        } else {
            paymentTransactionService.finalizePayment(payment.getId(), transactionId, PaymentStatus.FAILED);
            log.info("Payment failed for order: {}", orderId);
            throw new RuntimeException("Payment failed");
        }
    }
}
