package com.example.api.module.payment.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.example.core.domain.cart.Cart;
import com.example.core.domain.order.Order;
import com.example.core.domain.order.api.OrderApiRepository;
import com.example.core.domain.payment.Payment;
import com.example.core.domain.payment.api.PaymentApiRepository;
import com.example.core.domain.shipping_address.ShippingAddress;
import com.example.core.domain.user.User;
import com.example.core.dto.PaymentRequestDto;
import com.example.core.enums.OrderStatus;
import com.example.core.enums.PaymentMethod;
import com.example.core.enums.PaymentStatus;
import com.example.core.exception.BadRequestException;

@SpringBootTest
@ActiveProfiles({"api-test", "core-test"})
@Transactional
class PaymentTransactionServiceTest {

    @Autowired
    private PaymentTransactionService paymentTransactionService;

    @MockitoBean
    private OrderApiRepository orderApiRepository;
    @MockitoBean
    private PaymentApiRepository paymentApiRepository;

    private User testUser;
    private ShippingAddress testShippingAddress;
    private Order testOrder;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        // 테스트 장바구니 생성
        Cart cart = new Cart();
        cart.setId(1L);

        // 테스트 유저 생성
        testUser = User.of(
                "test@example.com",
                "Test User",
                "testSalt",
                "hashedPassword",
                "010-1234-5678",
                cart
        );
        testUser.setId(1L);
        cart.setUser(testUser);

        // 테스트 배송지 생성
        testShippingAddress = ShippingAddress.of(
                "Test User",
                "Test Address",
                "12345",
                "010-1234-5678",
                true
        );
        testShippingAddress.setId(1L);
        testShippingAddress.setUser(testUser);

        // 테스트 주문 생성
        testOrder = Order.of(testUser, testShippingAddress);
        testOrder.setId(1L);
        testOrder.setStatus(OrderStatus.CREATED);

        // 테스트 결제 생성
        testPayment = Payment.create(testOrder, createPaymentRequestDto());
        testPayment.setId(1L);
        testOrder.setPayment(testPayment);
    }

    // =========================== checkOrderForPayment 테스트 (중요도: 높음) ===========================

    @Test
    void checkOrderForPayment_성공() {
        // given
        when(orderApiRepository.findByIdAndUserId(testOrder.getId(), testUser.getId()))
                .thenReturn(Optional.of(testOrder));

        // when
        Order result = paymentTransactionService.checkOrderForPayment(testOrder.getId(), testUser.getId());

        // then
        assertNotNull(result);
        assertEquals(testOrder.getId(), result.getId());
        assertEquals(OrderStatus.PENDING, result.getStatus());
    }

    @Test
    void checkOrderForPayment_실패_존재하지않는주문() {
        // given
        when(orderApiRepository.findByIdAndUserId(testOrder.getId(), testUser.getId()))
                .thenReturn(Optional.empty());

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> paymentTransactionService.checkOrderForPayment(testOrder.getId(), testUser.getId()));
        assertEquals("주문을 찾을 수 없거나 소유자가 아닙니다.", exception.getMessage());
    }

    @Test
    void checkOrderForPayment_실패_다른유저주문() {
        // given
        Long wrongUserId = 999L;
        when(orderApiRepository.findByIdAndUserId(testOrder.getId(), wrongUserId))
                .thenReturn(Optional.empty());

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> paymentTransactionService.checkOrderForPayment(testOrder.getId(), wrongUserId));
        assertEquals("주문을 찾을 수 없거나 소유자가 아닙니다.", exception.getMessage());
    }

    // =========================== createPendingPayment 테스트 (중요도: 높음) ===========================

    @Test
    void createPendingPayment_성공() {
        // given
        PaymentRequestDto paymentRequestDto = createPaymentRequestDto();
        when(paymentApiRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(1L);
            return payment;
        });

        // when
        Payment payment = paymentTransactionService.createPendingPayment(testOrder, paymentRequestDto);

        // then
        assertNotNull(payment);
        assertEquals(testOrder.getId(), payment.getOrder().getId());
        assertEquals(PaymentStatus.PENDING, payment.getPaymentStatus());
        assertEquals(PaymentMethod.CARD, payment.getPaymentMethod());
        assertEquals("1234-5678-9012-3456", payment.getCardNumber());
        assertEquals(10000L, payment.getPaymentAmount());
    }

    @Test
    void createPendingPayment_성공_주문과결제연관관계설정() {
        // given
        PaymentRequestDto paymentRequestDto = createPaymentRequestDto();
        when(paymentApiRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(1L);
            return payment;
        });

        // when
        Payment payment = paymentTransactionService.createPendingPayment(testOrder, paymentRequestDto);

        // then
        assertNotNull(payment);
        assertEquals(testOrder.getId(), payment.getOrder().getId());
        assertEquals(PaymentStatus.PENDING, payment.getPaymentStatus());
        assertEquals(PaymentMethod.CARD, payment.getPaymentMethod());
        assertEquals("1234-5678-9012-3456", payment.getCardNumber());
        assertEquals(10000L, payment.getPaymentAmount());

        // 양방향 관계 확인
        assertNotNull(testOrder.getPayment());
        assertEquals(payment.getId(), testOrder.getPayment().getId());
    }

    // =========================== finalizePayment 테스트 (중요도: 높음) ===========================

    @Test
    void finalizePayment_성공_결제완료() {
        // given
        when(paymentApiRepository.findById(testPayment.getId())).thenReturn(Optional.of(testPayment));

        // when
        paymentTransactionService.finalizePayment(testPayment.getId(), "transaction123", PaymentStatus.COMPLETED);

        // then
        assertEquals(PaymentStatus.COMPLETED, testPayment.getPaymentStatus());
        assertEquals("transaction123", testPayment.getTransactionId());
    }

    @Test
    void finalizePayment_성공_결제실패() {
        // given
        when(paymentApiRepository.findById(testPayment.getId())).thenReturn(Optional.of(testPayment));

        // when
        paymentTransactionService.finalizePayment(testPayment.getId(), null, PaymentStatus.FAILED);

        // then
        assertEquals(PaymentStatus.FAILED, testPayment.getPaymentStatus());
        assertNull(testPayment.getTransactionId());
    }

    @Test
    void finalizePayment_성공_결제취소() {
        // given
        when(paymentApiRepository.findById(testPayment.getId())).thenReturn(Optional.of(testPayment));

        // when
        paymentTransactionService.finalizePayment(testPayment.getId(), "transaction123", PaymentStatus.CANCELLED);

        // then
        assertEquals(PaymentStatus.CANCELLED, testPayment.getPaymentStatus());
        assertEquals("transaction123", testPayment.getTransactionId());
    }

    @Test
    void finalizePayment_성공_존재하지않는결제ID() {
        // given
        when(paymentApiRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        // 존재하지 않는 결제 ID에 대해서는 아무 동작도 하지 않음
        assertDoesNotThrow(() -> paymentTransactionService.finalizePayment(999L, "transaction123", PaymentStatus.COMPLETED));
    }

    @Test
    void finalizePayment_다양한상태변경() {
        // given
        when(paymentApiRepository.findById(testPayment.getId())).thenReturn(Optional.of(testPayment));

        // when & then - PENDING -> COMPLETED
        paymentTransactionService.finalizePayment(testPayment.getId(), "transaction123", PaymentStatus.COMPLETED);
        assertEquals(PaymentStatus.COMPLETED, testPayment.getPaymentStatus());

        // when & then - COMPLETED -> CANCELLED
        paymentTransactionService.finalizePayment(testPayment.getId(), "transaction123", PaymentStatus.CANCELLED);
        assertEquals(PaymentStatus.CANCELLED, testPayment.getPaymentStatus());
    }

    private PaymentRequestDto createPaymentRequestDto() {
        return new PaymentRequestDto("1234-5678-9012-3456", 10000L, PaymentMethod.CARD);
    }
} 