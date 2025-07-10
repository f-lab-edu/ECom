package com.example.api.module.payment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.module.payment.externalPayment.service.ExternalPaymentService;
import com.example.core.domain.cart.Cart;
import com.example.core.domain.order.Order;
import com.example.core.domain.payment.Payment;
import com.example.core.domain.shipping_address.ShippingAddress;
import com.example.core.domain.user.User;
import com.example.core.dto.PaymentRequestDto;
import com.example.core.enums.PaymentMethod;
import com.example.core.enums.PaymentStatus;

@SpringBootTest
@ActiveProfiles({"api-test", "core-test"})
@Transactional
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @MockitoBean
    private ExternalPaymentService externalPaymentService;

    @MockitoBean
    private PaymentTransactionService paymentTransactionService;

    private User testUser;
    private Order testOrder;
    private Payment testPayment;
    private PaymentRequestDto testPaymentRequest;

    @BeforeEach
    void setUp() {
        // 테스트 유저 생성
        Cart cart = new Cart();
        testUser = User.builder()
                .email("test@example.com")
                .nickname("Test User")
                .salt("testSalt")
                .hashedPassword("hashedPassword")
                .phoneNumber("010-1234-5678")
                .cart(cart)
                .build();

        // 테스트 배송지 생성
        ShippingAddress shippingAddress = ShippingAddress.builder()
                .recipientName("Test Receiver")
                .address("Test Address")
                .zipCode("12345")
                .phoneNumber("010-1234-5678")
                .isDefault(true)
                .build();

        // 테스트 주문 생성
        testOrder = Order.builder()
                .user(testUser)
                .recipientName("Test Receiver")
                .address("Test Address")
                .zipCode("12345")
                .phoneNumber("010-1234-5678")
                .build();

        // 테스트 결제 생성
        testPayment = Payment.builder()
                .order(testOrder)
                .paymentAmount(20000L)
                .paymentMethod(PaymentMethod.CARD)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        // 테스트 결제 요청 생성 (reflection으로 필드 설정)
        testPaymentRequest = new PaymentRequestDto();
        try {
            java.lang.reflect.Field paymentAmountField = PaymentRequestDto.class.getDeclaredField("paymentAmount");
            paymentAmountField.setAccessible(true);
            paymentAmountField.set(testPaymentRequest, 20000L);
            
            java.lang.reflect.Field paymentMethodField = PaymentRequestDto.class.getDeclaredField("paymentMethod");
            paymentMethodField.setAccessible(true);
            paymentMethodField.set(testPaymentRequest, PaymentMethod.CARD);
            
            java.lang.reflect.Field cardNumberField = PaymentRequestDto.class.getDeclaredField("cardNumber");
            cardNumberField.setAccessible(true);
            cardNumberField.set(testPaymentRequest, "1234-5678-9012-3456");
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test payment request", e);
        }
    }

    // =========================== processPayment 테스트 (중요도: 높음) ===========================

    @Test
    void processPayment_성공() {
        // given
        String expectedTransactionId = "transaction123";

        when(paymentTransactionService.checkOrderForPayment(testOrder.getId(), testUser.getId()))
                .thenReturn(testOrder);
        when(paymentTransactionService.createPendingPayment(testOrder, testPaymentRequest))
                .thenReturn(testPayment);
        when(externalPaymentService.sendPaymentRequestIsSuccess(testPaymentRequest))
                .thenReturn(expectedTransactionId);

        // when
        String result = paymentService.processPayment(testUser.getId(), testOrder.getId(), testPaymentRequest);

        // then
        assertNotNull(result);
        assertEquals(expectedTransactionId, result);

        verify(paymentTransactionService, times(1)).checkOrderForPayment(testOrder.getId(), testUser.getId());
        verify(paymentTransactionService, times(1)).createPendingPayment(testOrder, testPaymentRequest);
        verify(externalPaymentService, times(1)).sendPaymentRequestIsSuccess(testPaymentRequest);
        verify(paymentTransactionService, times(1))
                .finalizePayment(testPayment.getId(), expectedTransactionId, PaymentStatus.COMPLETED);
    }

    @Test
    void processPayment_실패_외부결제서비스실패() {
        // given
        when(paymentTransactionService.checkOrderForPayment(testOrder.getId(), testUser.getId()))
                .thenReturn(testOrder);
        when(paymentTransactionService.createPendingPayment(testOrder, testPaymentRequest))
                .thenReturn(testPayment);
        when(externalPaymentService.sendPaymentRequestIsSuccess(testPaymentRequest))
                .thenReturn(null); // 결제 실패

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paymentService.processPayment(testUser.getId(), testOrder.getId(), testPaymentRequest));
        assertEquals("Payment failed", exception.getMessage());

        verify(paymentTransactionService, times(1))
                .finalizePayment(testPayment.getId(), null, PaymentStatus.FAILED);
    }

    @Test
    void processPayment_실패_주문검증실패() {
        // given
        when(paymentTransactionService.checkOrderForPayment(testOrder.getId(), testUser.getId()))
                .thenThrow(new RuntimeException("Order validation failed"));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paymentService.processPayment(testUser.getId(), testOrder.getId(), testPaymentRequest));
        assertEquals("Order validation failed", exception.getMessage());

        // 주문 검증 실패 시 결제 생성이나 외부 서비스 호출이 없어야 함
        verify(paymentTransactionService, times(0)).createPendingPayment(any(), any());
        verify(externalPaymentService, times(0)).sendPaymentRequestIsSuccess(any());
    }

    @Test
    void processPayment_실패_결제생성실패() {
        // given
        when(paymentTransactionService.checkOrderForPayment(testOrder.getId(), testUser.getId()))
                .thenReturn(testOrder);
        when(paymentTransactionService.createPendingPayment(testOrder, testPaymentRequest))
                .thenThrow(new RuntimeException("Payment creation failed"));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paymentService.processPayment(testUser.getId(), testOrder.getId(), testPaymentRequest));
        assertEquals("Payment creation failed", exception.getMessage());

        // 결제 생성 실패 시 외부 서비스 호출이 없어야 함
        verify(externalPaymentService, times(0)).sendPaymentRequestIsSuccess(any());
    }

    @Test
    void processPayment_실패_외부서비스예외() {
        // given
        when(paymentTransactionService.checkOrderForPayment(testOrder.getId(), testUser.getId()))
                .thenReturn(testOrder);
        when(paymentTransactionService.createPendingPayment(testOrder, testPaymentRequest))
                .thenReturn(testPayment);
        when(externalPaymentService.sendPaymentRequestIsSuccess(testPaymentRequest))
                .thenThrow(new RuntimeException("External service error"));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paymentService.processPayment(testUser.getId(), testOrder.getId(), testPaymentRequest));
        assertEquals("External service error", exception.getMessage());

        // 외부 서비스 예외 발생 시 결제 완료 처리가 호출되지 않아야 함
        verify(paymentTransactionService, times(0))
                .finalizePayment(eq(testPayment.getId()), any(String.class), eq(PaymentStatus.COMPLETED));
        verify(paymentTransactionService, times(0))
                .finalizePayment(eq(testPayment.getId()), any(String.class), eq(PaymentStatus.FAILED));
    }
} 