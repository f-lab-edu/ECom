package com.example.api.module.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

import com.example.api.module.coupon.service.CouponService;
import com.example.api.module.order.controller.request.OrderProductRequest;
import com.example.api.module.order.controller.response.OrderProductResponse;
import com.example.api.module.payment.service.PaymentService;
import com.example.api.module.stock.service.StockService;
import com.example.core.domain.cart.Cart;
import com.example.core.domain.cart.api.CartApiRepository;
import com.example.core.domain.cart_product.api.CartProductApiRepository;
import com.example.core.domain.category.Category;
import com.example.core.domain.category.api.CategoryApiRepository;
import com.example.core.domain.order.Order;
import com.example.core.domain.order.api.OrderApiRepository;
import com.example.core.domain.order_product.OrderProduct;
import com.example.core.domain.order_product.api.OrderProductApiRepository;
import com.example.core.domain.payment.Payment;
import com.example.core.domain.product.Product;
import com.example.core.domain.product.api.ProductApiRepository;
import com.example.core.domain.shipping_address.ShippingAddress;
import com.example.core.domain.shipping_address.api.ShippingAddressApiRepository;
import com.example.core.domain.user.User;
import com.example.core.domain.user.api.UserApiRepository;
import com.example.core.domain.user.meta.Status;
import com.example.core.dto.OrderProductRequestDto;
import com.example.core.dto.PaymentRequestDto;
import com.example.core.enums.OrderStatus;
import com.example.core.enums.PaymentMethod;
import com.example.core.exception.BadRequestException;

@SpringBootTest
@ActiveProfiles({"api-test", "core-test"})
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @MockBean
    private OrderTransactionService orderTransactionService;
    @MockBean
    private StockService stockService;
    @MockBean
    private PaymentService paymentService;
    @MockBean
    private CouponService couponService;

    @MockBean
    private UserApiRepository userApiRepository;
    @MockBean
    private ProductApiRepository productApiRepository;
    @MockBean
    private OrderApiRepository orderApiRepository;
    @MockBean
    private ShippingAddressApiRepository shippingAddressApiRepository;
    @MockBean
    private CartApiRepository cartApiRepository;
    @MockBean
    private CartProductApiRepository cartProductApiRepository;
    @MockBean
    private OrderProductApiRepository orderProductApiRepository;
    @MockBean
    private CategoryApiRepository categoryApiRepository;

    private Category testCategory;
    private Product testProduct;
    private User testUser;
    private ShippingAddress testShippingAddress;
    private Order testOrder;
    private OrderProduct testOrderProduct;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Test Category");

        testProduct = Product.of("Test Product", "description", 100L, 10000L, "thumbnail", testCategory);
        testProduct.setId(1L);

        Cart cart = new Cart();
        cart.setId(1L);

        testUser = User.of("test@example.com", "Test User", "salt", "password", "010-1234-5678", cart);
        testUser.setId(1L);
        cart.setUser(testUser);

        testShippingAddress = ShippingAddress.of("Test Recipient", "Test Address", "12345", "010-1111-2222", true);
        testShippingAddress.setId(1L);
        testShippingAddress.setUser(testUser);

        testOrder = Order.of(testUser, testShippingAddress);
        testOrder.setId(1L);

        testOrderProduct = OrderProduct.createOrderProduct(testOrder, testProduct, 1L, 10000L);
        testOrderProduct.setId(1L);

        testOrder.addOrderProduct(testOrderProduct);
        testUser.addOrder(testOrder);

        PaymentRequestDto paymentRequestDto = new PaymentRequestDto("1234-1234-1234-1234", 10000L, PaymentMethod.CARD);
        testPayment = Payment.create(testOrder, paymentRequestDto);
        testPayment.setId(1L);
        testOrder.setPayment(testPayment);
    }

    // =========================== getOrder 테스트 (중요도: 높음) ===========================

    @Test
    void getOrder_성공() {
        // given
        when(userApiRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(orderApiRepository.findById(testOrder.getId())).thenReturn(Optional.of(testOrder));

        // when
        OrderProductResponse result = orderService.getOrder(testUser.getId(), testOrder.getId());

        // then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(testOrder.getId(), result.getOrderDto().getOrderId());
        assertNotNull(result.getOrderProductDtos());
        assertEquals(1, result.getOrderProductDtos().size());
        assertEquals(testProduct.getId(), result.getOrderProductDtos().get(0).getProductId());
        assertEquals(1L, result.getOrderProductDtos().get(0).getQuantity());
        assertEquals(10000L, result.getOrderProductDtos().get(0).getPrice());
    }

    @Test
    void getOrder_실패_타인의주문조회() {
        // given
        User anotherUser = User.of(
                "another@example.com", "Another User", "anotherSalt",
                "anotherHashedPassword", "010-9876-5432", new Cart()
        );
        anotherUser.setId(99L);
        anotherUser.setStatus(Status.ACTIVE);

        when(userApiRepository.findById(anotherUser.getId())).thenReturn(Optional.of(anotherUser));
        when(orderApiRepository.findById(testOrder.getId())).thenReturn(Optional.of(testOrder));

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> orderService.getOrder(anotherUser.getId(), testOrder.getId()));
        assertEquals("not your order", exception.getMessage());
    }

    // =========================== getOrders 테스트 (중요도: 높음) ===========================

    @Test
    void getOrders_성공() {
        // given
        when(userApiRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(orderApiRepository.findById(testOrder.getId())).thenReturn(Optional.of(testOrder));

        // when
        List<OrderProductResponse> results = orderService.getOrders(testUser.getId());

        // then
        assertNotNull(results);
        assertEquals(1, results.size());
        
        OrderProductResponse result = results.get(0);
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(testOrder.getId(), result.getOrderDto().getOrderId());
        assertNotNull(result.getOrderProductDtos());
        assertEquals(1, result.getOrderProductDtos().size());
        assertEquals(testProduct.getId(), result.getOrderProductDtos().get(0).getProductId());
        assertEquals(1L, result.getOrderProductDtos().get(0).getQuantity());
        assertEquals(10000L, result.getOrderProductDtos().get(0).getPrice());
    }

    @Test
    void getOrders_실패_존재하지않는유저() {
        // given
        Long nonExistentUserId = 99999L;

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> orderService.getOrders(nonExistentUserId));
        assertEquals("user not found", exception.getMessage());
    }

    // =========================== orderProduct 테스트 (중요도: 높음) ===========================

    @Test
    void orderProduct_성공_기본주문() {
        // given
        OrderProductRequest request = createOrderProductRequest();
        Order mockOrder = createMockOrder();
        mockOrder.getOrderProducts().add(testOrderProduct);

        when(orderTransactionService.createOrder(eq(testUser.getId()), eq(request))).thenReturn(mockOrder);
        when(stockService.tryReserve(eq(mockOrder.getId()), eq(testProduct.getId()), eq(2L))).thenReturn(true);
        when(paymentService.processPayment(eq(testUser.getId()), eq(mockOrder.getId()), any(PaymentRequestDto.class)))
                .thenReturn("transaction123");
        doNothing().when(orderTransactionService).finalizeOrderSuccess(eq(mockOrder.getId()), eq(request));

        // when
        OrderProductResponse result = orderService.orderProduct(testUser.getId(), request);

        // then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(mockOrder.getId(), result.getOrderDto().getOrderId());
        assertNotNull(result.getOrderProductDtos());
        assertEquals(1, result.getOrderProductDtos().size());
        assertEquals(testProduct.getId(), result.getOrderProductDtos().get(0).getProductId());
        assertEquals(1L, result.getOrderProductDtos().get(0).getQuantity());
        assertEquals(10000L, result.getOrderProductDtos().get(0).getPrice());

        verify(orderTransactionService).createOrder(eq(testUser.getId()), eq(request));
        verify(stockService).tryReserve(eq(mockOrder.getId()), eq(testProduct.getId()), eq(2L));
        verify(paymentService).processPayment(eq(testUser.getId()), eq(mockOrder.getId()), any(PaymentRequestDto.class));
        verify(orderTransactionService).finalizeOrderSuccess(eq(mockOrder.getId()), eq(request));
    }

    @Test
    void orderProduct_성공_쿠폰적용주문() {
        // given
        OrderProductRequest request = createOrderProductRequestWithCoupon();
        Order mockOrder = createMockOrder();
        mockOrder.getOrderProducts().add(testOrderProduct);

        when(orderTransactionService.createOrder(eq(testUser.getId()), eq(request))).thenReturn(mockOrder);
        when(stockService.tryReserve(eq(mockOrder.getId()), eq(testProduct.getId()), eq(2L))).thenReturn(true);
        when(couponService.tryReserve(eq(testUser.getId()), eq(mockOrder.getId()), eq(1L))).thenReturn(true);
        when(paymentService.processPayment(eq(testUser.getId()), eq(mockOrder.getId()), any(PaymentRequestDto.class)))
                .thenReturn("transaction123");
        doNothing().when(orderTransactionService).finalizeOrderSuccess(eq(mockOrder.getId()), eq(request));

        // when
        OrderProductResponse result = orderService.orderProduct(testUser.getId(), request);

        // then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(mockOrder.getId(), result.getOrderDto().getOrderId());
        assertNotNull(result.getOrderProductDtos());
        assertEquals(1, result.getOrderProductDtos().size());
        assertEquals(testProduct.getId(), result.getOrderProductDtos().get(0).getProductId());
        assertEquals(1L, result.getOrderProductDtos().get(0).getQuantity());
        assertEquals(10000L, result.getOrderProductDtos().get(0).getPrice());

        verify(orderTransactionService).createOrder(eq(testUser.getId()), eq(request));
        verify(stockService).tryReserve(eq(mockOrder.getId()), eq(testProduct.getId()), eq(2L));
        verify(couponService).tryReserve(eq(testUser.getId()), eq(mockOrder.getId()), eq(1L));
        verify(paymentService).processPayment(eq(testUser.getId()), eq(mockOrder.getId()), any(PaymentRequestDto.class));
        verify(orderTransactionService).finalizeOrderSuccess(eq(mockOrder.getId()), eq(request));
    }

    @Test
    void orderProduct_실패_재고부족() {
        // given
        OrderProductRequest request = createOrderProductRequest();
        Order mockOrder = createMockOrder();
        mockOrder.getOrderProducts().add(testOrderProduct);

        when(orderTransactionService.createOrder(eq(testUser.getId()), eq(request))).thenReturn(mockOrder);
        when(stockService.tryReserve(eq(mockOrder.getId()), eq(testProduct.getId()), eq(2L))).thenReturn(false);
        doNothing().when(stockService).revertReservation(eq(mockOrder.getId()), eq(testProduct.getId()));

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> orderService.orderProduct(testUser.getId(), request));
        assertEquals("order failed: stock not enough for product: " + testProduct.getId(), exception.getMessage());

        verify(orderTransactionService).createOrder(eq(testUser.getId()), eq(request));
        verify(stockService).tryReserve(eq(mockOrder.getId()), eq(testProduct.getId()), eq(2L));
        verify(paymentService, never()).processPayment(any(), any(), any());
        verify(orderTransactionService, never()).finalizeOrderSuccess(any(), any());
    }

    @Test
    void orderProduct_실패_쿠폰사용불가() {
        // given
        OrderProductRequest request = createOrderProductRequestWithCoupon();
        Order mockOrder = createMockOrder();
        mockOrder.getOrderProducts().add(testOrderProduct);

        when(orderTransactionService.createOrder(eq(testUser.getId()), eq(request))).thenReturn(mockOrder);
        when(stockService.tryReserve(eq(mockOrder.getId()), eq(testProduct.getId()), eq(2L))).thenReturn(true);
        when(couponService.tryReserve(eq(testUser.getId()), eq(mockOrder.getId()), eq(1L))).thenReturn(false);
        doNothing().when(stockService).revertReservation(eq(mockOrder.getId()), eq(testProduct.getId()));
        doNothing().when(couponService).revertReservation(eq(mockOrder.getId()), eq(1L));

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> orderService.orderProduct(testUser.getId(), request));
        assertEquals("order failed: coupon not available or already used", exception.getMessage());

        verify(orderTransactionService).createOrder(eq(testUser.getId()), eq(request));
        verify(stockService).tryReserve(eq(mockOrder.getId()), eq(testProduct.getId()), eq(2L));
        verify(couponService).tryReserve(eq(testUser.getId()), eq(mockOrder.getId()), eq(1L));
        verify(stockService).revertReservation(eq(mockOrder.getId()), eq(testProduct.getId()));
        verify(paymentService, never()).processPayment(any(), any(), any());
        verify(orderTransactionService, never()).finalizeOrderSuccess(any(), any());
    }

    @Test
    void orderProduct_실패_결제실패() {
        // given
        OrderProductRequest request = createOrderProductRequest();
        Order mockOrder = createMockOrder();

        when(orderTransactionService.createOrder(eq(testUser.getId()), eq(request))).thenReturn(mockOrder);
        when(stockService.tryReserve(eq(mockOrder.getId()), eq(testProduct.getId()), eq(2L))).thenReturn(true);
        when(paymentService.processPayment(eq(testUser.getId()), eq(mockOrder.getId()), any(PaymentRequestDto.class)))
                .thenThrow(new RuntimeException("Payment failed"));

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> orderService.orderProduct(testUser.getId(), request));
        assertEquals("order failed: Payment failed", exception.getMessage());

        verify(stockService, times(1)).revertReservation(mockOrder.getId(), testProduct.getId());
    }

    // Helper methods
    private OrderProductRequest createOrderProductRequest() {
        OrderProductRequest request = new OrderProductRequest();
        
        // Create DTOs
        OrderProductRequestDto dto = new OrderProductRequestDto();
        setField(dto, "productId", testProduct.getId());
        setField(dto, "quantity", 2L);
        
        PaymentRequestDto paymentDto = new PaymentRequestDto();
        setField(paymentDto, "paymentMethod", PaymentMethod.CARD);
        setField(paymentDto, "cardNumber", "1234-5678-9012-3456");
        setField(paymentDto, "paymentAmount", 20000L);
        
        // Set fields in request
        setField(request, "orderProductDtos", List.of(dto));
        setField(request, "paymentRequestDto", paymentDto);
        setField(request, "shippingAddressId", testShippingAddress.getId());
        
        return request;
    }
    
    private OrderProductRequest createOrderProductRequestWithCoupon() {
        OrderProductRequest request = createOrderProductRequest();
        setField(request, "couponId", 1L);
        return request;
    }

    private Order createMockOrder() {
        Order order = Order.of(testUser, testShippingAddress);
        order.setId(1L);
        order.setStatus(OrderStatus.CREATED);
        Payment payment = Payment.builder().id(1L).build();
        order.setPayment(payment);
        return order;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
} 