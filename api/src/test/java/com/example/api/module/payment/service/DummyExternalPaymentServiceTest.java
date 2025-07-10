package com.example.api.module.payment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.module.payment.externalPayment.service.impl.DummyExternalPaymentService;
import com.example.core.dto.PaymentRequestDto;
import com.example.core.enums.PaymentMethod;

@SpringBootTest
@ActiveProfiles({"api-test", "core-test"})
@Transactional
class DummyExternalPaymentServiceTest {

    @Autowired
    private DummyExternalPaymentService dummyExternalPaymentService;

    private PaymentRequestDto testPaymentRequest;

    @BeforeEach
    void setUp() {
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

    // =========================== sendPaymentRequestIsSuccess 테스트 (중요도: 높음) ===========================

    @Test
    void sendPaymentRequestIsSuccess_성공_유효한결제정보() {
        // given
        // 유효한 결제 정보로 성공 시나리오

        // when
        String result = dummyExternalPaymentService.sendPaymentRequestIsSuccess(testPaymentRequest);

        // then
        // 더미 서비스는 항상 transaction ID를 반환
        assertNotNull(result);
        assertEquals("RANDOM_TRANSACTION_ID", result);
    }

    @Test
    void sendPaymentRequestIsSuccess_성공_높은금액() {
        // given
        try {
            java.lang.reflect.Field paymentAmountField = PaymentRequestDto.class.getDeclaredField("paymentAmount");
            paymentAmountField.setAccessible(true);
            paymentAmountField.set(testPaymentRequest, 1000000L); // 100만원
        } catch (Exception e) {
            throw new RuntimeException("Failed to set payment amount", e);
        }

        // when
        String result = dummyExternalPaymentService.sendPaymentRequestIsSuccess(testPaymentRequest);

        // then
        // 더미 서비스는 금액에 관계없이 동일한 transaction ID를 반환
        assertNotNull(result);
        assertEquals("RANDOM_TRANSACTION_ID", result);
    }

    @Test
    void sendPaymentRequestIsSuccess_성공_낮은금액() {
        // given
        try {
            java.lang.reflect.Field paymentAmountField = PaymentRequestDto.class.getDeclaredField("paymentAmount");
            paymentAmountField.setAccessible(true);
            paymentAmountField.set(testPaymentRequest, 100L); // 100원
        } catch (Exception e) {
            throw new RuntimeException("Failed to set payment amount", e);
        }

        // when
        String result = dummyExternalPaymentService.sendPaymentRequestIsSuccess(testPaymentRequest);

        // then
        // 더미 서비스는 금액에 관계없이 동일한 transaction ID를 반환
        assertNotNull(result);
        assertEquals("RANDOM_TRANSACTION_ID", result);
    }

    @Test
    void sendPaymentRequestIsSuccess_성공_다양한결제수단() {
        // given - 카드 결제
        try {
            java.lang.reflect.Field paymentMethodField = PaymentRequestDto.class.getDeclaredField("paymentMethod");
            paymentMethodField.setAccessible(true);
            paymentMethodField.set(testPaymentRequest, PaymentMethod.CARD);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set payment method", e);
        }

        // when
        String cardResult = dummyExternalPaymentService.sendPaymentRequestIsSuccess(testPaymentRequest);

        // then
        assertNotNull(cardResult);
        assertEquals("RANDOM_TRANSACTION_ID", cardResult);

        // given - 카카오페이 결제
        try {
            java.lang.reflect.Field paymentMethodField = PaymentRequestDto.class.getDeclaredField("paymentMethod");
            paymentMethodField.setAccessible(true);
            paymentMethodField.set(testPaymentRequest, PaymentMethod.KAKAOPAY);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set payment method", e);
        }

        // when
        String kakaoResult = dummyExternalPaymentService.sendPaymentRequestIsSuccess(testPaymentRequest);

        // then
        assertNotNull(kakaoResult);
        assertEquals("RANDOM_TRANSACTION_ID", kakaoResult);
    }

    @Test
    void sendPaymentRequestIsSuccess_성공_다양한카드번호() {
        // given - 첫 번째 카드번호
        try {
            java.lang.reflect.Field cardNumberField = PaymentRequestDto.class.getDeclaredField("cardNumber");
            cardNumberField.setAccessible(true);
            cardNumberField.set(testPaymentRequest, "1111-2222-3333-4444");
        } catch (Exception e) {
            throw new RuntimeException("Failed to set card number", e);
        }

        // when
        String result1 = dummyExternalPaymentService.sendPaymentRequestIsSuccess(testPaymentRequest);

        // then
        assertNotNull(result1);
        assertEquals("RANDOM_TRANSACTION_ID", result1);

        // given - 두 번째 카드번호
        try {
            java.lang.reflect.Field cardNumberField = PaymentRequestDto.class.getDeclaredField("cardNumber");
            cardNumberField.setAccessible(true);
            cardNumberField.set(testPaymentRequest, "9999-8888-7777-6666");
        } catch (Exception e) {
            throw new RuntimeException("Failed to set card number", e);
        }

        // when
        String result2 = dummyExternalPaymentService.sendPaymentRequestIsSuccess(testPaymentRequest);

        // then
        assertNotNull(result2);
        assertEquals("RANDOM_TRANSACTION_ID", result2);
    }

    @Test
    void sendPaymentRequestIsSuccess_성공_null값아닌유효객체() {
        // given
        // PaymentRequestDto 객체가 null이 아닌 경우

        // when
        String result = dummyExternalPaymentService.sendPaymentRequestIsSuccess(testPaymentRequest);

        // then
        assertNotNull(result);
        assertEquals("RANDOM_TRANSACTION_ID", result);
    }

    // 실제 서비스에서는 외부 API 호출 실패 시나리오가 있겠지만,
    // 이는 더미 서비스이므로 항상 성공을 반환한다.
    // 만약 실제 외부 결제 서비스 구현 시에는 다음과 같은 테스트들이 필요할 것:
    // - 네트워크 오류 시나리오
    // - 외부 API 응답 오류 시나리오
    // - 인증 실패 시나리오
    // - 결제 승인 거부 시나리오

    @Test
    void sendPaymentRequestIsSuccess_더미서비스특성_항상성공() {
        // given
        // 다양한 결제 시나리오들을 빠르게 테스트

        // when & then - 첫 번째 시도
        String result1 = dummyExternalPaymentService.sendPaymentRequestIsSuccess(testPaymentRequest);
        assertNotNull(result1);
        assertEquals("RANDOM_TRANSACTION_ID", result1);

        // when & then - 두 번째 시도 (연속 호출)
        String result2 = dummyExternalPaymentService.sendPaymentRequestIsSuccess(testPaymentRequest);
        assertNotNull(result2);
        assertEquals("RANDOM_TRANSACTION_ID", result2);

        // when & then - 세 번째 시도 (반복 호출)
        String result3 = dummyExternalPaymentService.sendPaymentRequestIsSuccess(testPaymentRequest);
        assertNotNull(result3);
        assertEquals("RANDOM_TRANSACTION_ID", result3);
    }

    @Test
    void sendPaymentRequestIsSuccess_성공_일관된반환값() {
        // given
        // 더미 서비스는 항상 동일한 값을 반환해야 함

        // when
        String result1 = dummyExternalPaymentService.sendPaymentRequestIsSuccess(testPaymentRequest);
        String result2 = dummyExternalPaymentService.sendPaymentRequestIsSuccess(testPaymentRequest);
        String result3 = dummyExternalPaymentService.sendPaymentRequestIsSuccess(testPaymentRequest);

        // then
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
        assertEquals(result1, result2);
        assertEquals(result2, result3);
        assertEquals("RANDOM_TRANSACTION_ID", result1);
    }

    // 추가적으로 실제 구현에서는 mock을 사용하여 실패 시나리오를 테스트할 수 있음
    // 예를 들어:
    // @Test
    // void sendPaymentRequestIsSuccess_실패_외부서비스오류() {
    //     // Mock external service to return null or throw exception
    //     // Test service recovery logic
    //     // Test retry mechanisms
    //     // Test error handling
    // }
} 