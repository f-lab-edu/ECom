package com.example.api.module.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.module.order.controller.request.OrderProductRequest;
import com.example.core.domain.cart.Cart;
import com.example.core.domain.category.Category;
import com.example.core.domain.coupon.api.CouponApiRepository;
import com.example.core.domain.order.Order;
import com.example.core.domain.order.api.OrderApiRepository;
import com.example.core.domain.order_product.OrderProduct;
import com.example.core.domain.order_product.api.OrderProductApiRepository;
import com.example.core.domain.payment.api.PaymentApiRepository;
import com.example.core.domain.product.Product;
import com.example.core.domain.product.api.ProductApiRepository;
import com.example.core.domain.shipping_address.ShippingAddress;
import com.example.core.domain.shipping_address.api.ShippingAddressApiRepository;
import com.example.core.domain.user.User;
import com.example.core.domain.user.api.UserApiRepository;
import com.example.core.dto.OrderProductRequestDto;
import com.example.core.dto.PaymentRequestDto;
import com.example.core.enums.OrderStatus;
import com.example.core.enums.PaymentMethod;
import com.example.core.exception.BadRequestException;

@SpringBootTest
@ActiveProfiles({"api-test", "core-test"})
@Transactional
class OrderTransactionServiceTest {

    @Autowired
    private OrderTransactionService orderTransactionService;

    @MockBean
    private UserApiRepository userApiRepository;
    @MockBean
    private ProductApiRepository productApiRepository;
    @MockBean
    private OrderApiRepository orderApiRepository;
    @MockBean
    private ShippingAddressApiRepository shippingAddressApiRepository;
    @MockBean
    private OrderProductApiRepository orderProductApiRepository;
    @MockBean
    private CouponApiRepository couponApiRepository;
    @MockBean
    private PaymentApiRepository paymentApiRepository;


    private User testUser;
    private Product testProduct;
    private Category testCategory;
    private ShippingAddress testShippingAddress;
    private Order testOrder;
    private OrderProduct testOrderProduct;

    @BeforeEach
    void setUp() {
        // 테스트 카테고리 생성
        testCategory = new Category();
        testCategory.setName("Test Category");
        testCategory.setId(1L);

        // 테스트 상품 생성
        testProduct = Product.of(
                "Test Product",
                "Test Description",
                100L,
                10000L,
                "http://test-thumbnail.com",
                testCategory
        );
        testProduct.setId(1L);

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

        // 테스트 주문상품 생성
        testOrderProduct = OrderProduct.createOrderProduct(testOrder, testProduct, 1L, testProduct.getPrice());
        testOrderProduct.setId(1L);
        testOrder.addOrderProduct(testOrderProduct);
        testUser.addOrder(testOrder);
    }

    @Test
    void createOrder_성공() {
        // given
        OrderProductRequest request = createOrderProductRequest();
        when(userApiRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(shippingAddressApiRepository.findById(testShippingAddress.getId())).thenReturn(Optional.of(testShippingAddress));
        when(productApiRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));
        when(orderApiRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });
        when(orderProductApiRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Order order = orderTransactionService.createOrder(testUser.getId(), request);

        // then
        assertNotNull(order);
        assertEquals(testUser.getId(), order.getUser().getId());
        assertEquals(testShippingAddress.getRecipientName(), order.getRecipientName());
        assertEquals(testShippingAddress.getAddress(), order.getAddress());
        assertEquals(testShippingAddress.getZipCode(), order.getZipCode());
        assertEquals(testShippingAddress.getPhoneNumber(), order.getPhoneNumber());
        assertEquals(OrderStatus.CREATED, order.getStatus());
        assertNotNull(order.getOrderProducts());
        assertEquals(1, order.getOrderProducts().size());
        assertEquals(testProduct.getId(), order.getOrderProducts().get(0).getProduct().getId());
        assertEquals(2L, order.getOrderProducts().get(0).getQuantity());
        assertEquals(10000L, order.getOrderProducts().get(0).getProductPrice());
    }

    @Test
    void createOrder_실패_존재하지않는상품() {
        // given
        OrderProductRequest request = createOrderProductRequest();
        when(userApiRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(shippingAddressApiRepository.findById(anyLong())).thenReturn(Optional.of(testShippingAddress));
        when(productApiRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> orderTransactionService.createOrder(testUser.getId(), request));
        assertEquals("product not found", exception.getMessage());
    }

    @Test
    void createOrder_실패_존재하지않는유저() {
        // given
        OrderProductRequest request = createOrderProductRequest();
        when(userApiRepository.findById(testUser.getId())).thenReturn(Optional.empty());

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> orderTransactionService.createOrder(testUser.getId(), request));
        assertEquals("user not found", exception.getMessage(), "Exception message for non-existent user does not match");

        verify(userApiRepository).findById(testUser.getId());
        verify(orderApiRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_실패_존재하지않는배송지() {
        // given
        OrderProductRequest request = createOrderProductRequest();
        when(userApiRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(shippingAddressApiRepository.findById(testShippingAddress.getId())).thenReturn(Optional.empty());

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> orderTransactionService.createOrder(testUser.getId(), request));
        assertEquals("shipping address not found", exception.getMessage(), "Exception message for non-existent address does not match");

        verify(userApiRepository).findById(testUser.getId());
        verify(shippingAddressApiRepository).findById(testShippingAddress.getId());
        verify(orderApiRepository, never()).save(any(Order.class));
    }

    private OrderProductRequest createOrderProductRequest() {
        OrderProductRequestDto dto = new OrderProductRequestDto(testProduct.getId(), 2L);
        PaymentRequestDto paymentDto = new PaymentRequestDto("1234-5678-9012-3456", 20000L, PaymentMethod.CARD);
        return new OrderProductRequest(List.of(dto), paymentDto, null, testShippingAddress.getId());
    }
} 