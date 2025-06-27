package com.example.api.module.payment.service;

import com.example.api.module.payment.externalPayment.service.ExternalPaymentService;
import com.example.core.domain.order.Order;
import com.example.core.domain.order.api.OrderApiRepository;
import com.example.core.domain.payment.Payment;
import com.example.core.domain.payment.api.PaymentApiRepository;
import com.example.core.dto.PaymentRequestDto;
import com.example.core.enums.OrderStatus;
import com.example.core.enums.PaymentStatus;
import com.example.core.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentTransactionService {
    private final OrderApiRepository orderApiRepository;
    private final PaymentApiRepository paymentApiRepository;

    @Transactional(readOnly = true)
    public Order checkOrderForPayment(Long orderId, Long userId) {
        Order order = orderApiRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new BadRequestException("주문을 찾을 수 없거나 소유자가 아닙니다."));
        order.setStatus(OrderStatus.PENDING);

        // PENDING 또는 AWAITING_PAYMENT 와 같은 결제 대기 상태가 아닐 경우 중복 결제 시도임
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("이미 처리되었거나 결제가 불가능한 주문입니다. 상태: " + order.getStatus());
        }
        return order;
    }

    /**
     * [DB 작업 2] 결제 시도를 기록하기 위해 Payment 엔티티를 PENDING 상태로 생성
     */
    @Transactional
    public Payment createPendingPayment(Order order, PaymentRequestDto paymentRequestDto) {
        Payment payment = Payment.create(order, paymentRequestDto);
        order.setPayment(payment);
        return paymentApiRepository.save(payment);
    }

    /**
     * [DB 작업 3] 외부 결제 결과(성공/실패)를 Payment 엔티티에 최종 반영
     */
    @Transactional
    public void finalizePayment(Long paymentId, String transactionId, PaymentStatus status) {
        paymentApiRepository.findById(paymentId).ifPresent(payment -> {
            payment.setStatus(status);
            payment.setTransactionId(transactionId); // 외부 PG사 거래 ID 저장
        });
    }
}
