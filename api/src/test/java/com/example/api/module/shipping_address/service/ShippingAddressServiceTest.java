package com.example.api.module.shipping_address.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.module.shipping_address.controller.request.ShippingAddressRequest;
import com.example.api.module.shipping_address.controller.response.ShippingAddressResponse;
import com.example.core.domain.cart.Cart;
import com.example.core.domain.shipping_address.ShippingAddress;
import com.example.core.domain.shipping_address.api.ShippingAddressApiRepository;
import com.example.core.domain.user.User;
import com.example.core.domain.user.api.UserApiRepository;

@SpringBootTest
@ActiveProfiles({"api-test", "core-test"})
@Transactional
class ShippingAddressServiceTest {

    @Autowired
    private ShippingAddressService shippingAddressService;

    @MockitoBean
    private ShippingAddressApiRepository shippingAddressApiRepository;

    @MockitoBean
    private UserApiRepository userApiRepository;

    private User testUser;
    private ShippingAddress testShippingAddress1;
    private ShippingAddress testShippingAddress2;
    private ShippingAddressRequest testRequest;
    private Long testUserId = 1L;
    private Long testAddressId = 1L;

    @BeforeEach
    void setUp() {
        // 테스트 유저 생성
        Cart cart = new Cart();
        testUser = User.builder()
                .id(testUserId)
                .email("test@example.com")
                .nickname("Test User")
                .salt("testSalt")
                .hashedPassword("hashedPassword")
                .phoneNumber("010-1234-5678")
                .cart(cart)
                .build();

        // 테스트 배송주소 1 (기본 주소)
        testShippingAddress1 = ShippingAddress.builder()
                .id(testAddressId)
                .recipientName("Test Receiver 1")
                .address("Test Address 1")
                .zipCode("12345")
                .phoneNumber("010-1234-5678")
                .isDefault(true)
                .user(testUser)
                .build();

        // 테스트 배송주소 2 (일반 주소)
        testShippingAddress2 = ShippingAddress.builder()
                .id(2L)
                .recipientName("Test Receiver 2")
                .address("Test Address 2")
                .zipCode("54321")
                .phoneNumber("010-8765-4321")
                .isDefault(false)
                .user(testUser)
                .build();

        // 테스트 요청 생성
        testRequest = new ShippingAddressRequest();
        testRequest.setRecipientName("New Receiver");
        testRequest.setAddress("New Address");
        testRequest.setZipCode("11111");
        testRequest.setPhoneNumber("010-9999-8888");
    }

    // =========================== getShippingAddresses 테스트 (중요도: 높음) ===========================

    @Test
    void getShippingAddresses_성공() {
        // given
        List<ShippingAddress> addresses = Arrays.asList(testShippingAddress1, testShippingAddress2);
        when(shippingAddressApiRepository.findAllByUserId(testUserId)).thenReturn(addresses);

        // when
        List<ShippingAddressResponse> result = shippingAddressService.getShippingAddresses(testUserId);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test Receiver 1", result.get(0).getRecipientName());
        assertEquals("Test Receiver 2", result.get(1).getRecipientName());
        assertTrue(result.get(0).isDefault());
        assertFalse(result.get(1).isDefault());
    }

    @Test
    void getShippingAddresses_성공_빈목록() {
        // given
        when(shippingAddressApiRepository.findAllByUserId(testUserId)).thenReturn(Collections.emptyList());

        // when
        List<ShippingAddressResponse> result = shippingAddressService.getShippingAddresses(testUserId);

        // then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // =========================== createShippingAddress 테스트 (중요도: 높음) ===========================

    @Test
    void createShippingAddress_성공_첫번째주소() {
        // given
        when(userApiRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(shippingAddressApiRepository.countByUserId(testUserId)).thenReturn(0L);
        when(shippingAddressApiRepository.save(any(ShippingAddress.class))).thenReturn(testShippingAddress1);

        // when
        ShippingAddressResponse result = shippingAddressService.createShippingAddress(testUserId, testRequest);

        // then
        assertNotNull(result);
        verify(shippingAddressApiRepository, times(1)).save(any(ShippingAddress.class));
        // 첫 번째 주소는 자동으로 기본 주소가 되어야 함
    }

    @Test
    void createShippingAddress_성공_추가주소() {
        // given
        when(userApiRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(shippingAddressApiRepository.countByUserId(testUserId)).thenReturn(1L);
        when(shippingAddressApiRepository.save(any(ShippingAddress.class))).thenReturn(testShippingAddress2);

        // when
        ShippingAddressResponse result = shippingAddressService.createShippingAddress(testUserId, testRequest);

        // then
        assertNotNull(result);
        verify(shippingAddressApiRepository, times(1)).save(any(ShippingAddress.class));
        // 추가 주소는 기본 주소가 아니어야 함
    }

    @Test
    void createShippingAddress_실패_존재하지않는유저() {
        // given
        when(userApiRepository.findById(testUserId)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> shippingAddressService.createShippingAddress(testUserId, testRequest));
        assertEquals("User not found", exception.getMessage());
    }

    // =========================== updateShippingAddress 테스트 (중요도: 높음) ===========================

    @Test
    void updateShippingAddress_성공() {
        // given
        when(shippingAddressApiRepository.findByIdAndUserId(testAddressId, testUserId))
                .thenReturn(Optional.of(testShippingAddress1));
        when(shippingAddressApiRepository.save(any(ShippingAddress.class))).thenReturn(testShippingAddress1);

        // when
        ShippingAddressResponse result = shippingAddressService.updateShippingAddress(testUserId, testAddressId, testRequest);

        // then
        assertNotNull(result);
        verify(shippingAddressApiRepository, times(1)).save(testShippingAddress1);
        assertEquals("New Receiver", testShippingAddress1.getRecipientName());
        assertEquals("New Address", testShippingAddress1.getAddress());
        assertEquals("11111", testShippingAddress1.getZipCode());
        assertEquals("010-9999-8888", testShippingAddress1.getPhoneNumber());
    }

    @Test
    void updateShippingAddress_실패_존재하지않는주소() {
        // given
        when(shippingAddressApiRepository.findByIdAndUserId(testAddressId, testUserId))
                .thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> shippingAddressService.updateShippingAddress(testUserId, testAddressId, testRequest));
        assertEquals("Shipping address not found", exception.getMessage());
    }

    // =========================== setDefaultShippingAddress 테스트 (중요도: 높음) ===========================

    @Test
    void setDefaultShippingAddress_성공() {
        // given
        List<ShippingAddress> userAddresses = Arrays.asList(testShippingAddress1, testShippingAddress2);
        when(shippingAddressApiRepository.findByIdAndUserId(2L, testUserId))
                .thenReturn(Optional.of(testShippingAddress2));
        when(shippingAddressApiRepository.findAllByUserId(testUserId)).thenReturn(userAddresses);

        // when
        ShippingAddressResponse result = shippingAddressService.setDefaultShippingAddress(testUserId, 2L);

        // then
        assertNotNull(result);
        assertFalse(testShippingAddress1.getIsDefault()); // 기존 기본 주소는 false
        assertTrue(testShippingAddress2.getIsDefault());   // 새로운 기본 주소는 true
    }

    @Test
    void setDefaultShippingAddress_실패_존재하지않는주소() {
        // given
        when(shippingAddressApiRepository.findByIdAndUserId(testAddressId, testUserId))
                .thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> shippingAddressService.setDefaultShippingAddress(testUserId, testAddressId));
        assertEquals("Shipping address not found", exception.getMessage());
    }

    // =========================== deleteShippingAddress 테스트 (중요도: 높음) ===========================

    @Test
    void deleteShippingAddress_성공() {
        // given
        when(shippingAddressApiRepository.findByIdAndUserId(testAddressId, testUserId))
                .thenReturn(Optional.of(testShippingAddress1));

        // when
        shippingAddressService.deleteShippingAddress(testUserId, testAddressId);

        // then
        verify(shippingAddressApiRepository, times(1)).delete(testShippingAddress1);
    }

    @Test
    void deleteShippingAddress_실패_존재하지않는주소() {
        // given
        when(shippingAddressApiRepository.findByIdAndUserId(testAddressId, testUserId))
                .thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> shippingAddressService.deleteShippingAddress(testUserId, testAddressId));
        assertEquals("Shipping address not found", exception.getMessage());
    }
} 