package com.example.api.module.cart.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.module.cart.controller.request.CartProductAddRequest;
import com.example.api.module.cart.controller.request.CartProductQuantityUpdateRequest;
import com.example.api.module.cart.controller.response.CartSummaryResponse;
import com.example.core.domain.cart.Cart;
import com.example.core.domain.cart.api.CartApiRepository;
import com.example.core.domain.cart_product.CartProduct;
import com.example.core.domain.cart_product.api.CartProductApiRepository;
import com.example.core.domain.category.Category;
import com.example.core.domain.category.api.CategoryApiRepository;
import com.example.core.domain.product.Product;
import com.example.core.domain.product.api.ProductApiRepository;
import com.example.core.domain.user.User;
import com.example.core.domain.user.api.UserApiRepository;
import com.example.core.exception.BadRequestException;

@SpringBootTest
@ActiveProfiles({"api-test", "core-test"})
@Transactional
class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserApiRepository userApiRepository;

    @Autowired
    private ProductApiRepository productApiRepository;

    @Autowired
    private CategoryApiRepository categoryApiRepository;

    @Autowired
    private CartProductApiRepository cartProductApiRepository;

    @Autowired
    private CartApiRepository cartApiRepository;

    private User testUser;
    private Product testProduct;
    private Category testCategory;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        // 테스트 카테고리 생성
        testCategory = new Category();
        testCategory.setName("Test Category");
        testCategory = categoryApiRepository.save(testCategory);

        // 테스트 상품 생성
        testProduct = Product.of(
                "Test Product",
                "Test Description",
                100L,
                10000L,
                "http://test-thumbnail.com",
                testCategory
        );
        testProduct = productApiRepository.save(testProduct);

        // 테스트 장바구니 생성 및 저장
        testCart = new Cart();
        testCart = cartApiRepository.save(testCart);

        // 테스트 유저 생성 및 저장
        testUser = User.of(
                "test@example.com",
                "Test User",
                "testSalt",
                "hashedTestPassword",
                "010-1234-5678",
                testCart
        );
        testUser = userApiRepository.save(testUser);
    }

    // =========================== getCart 테스트 (중요도: 높음) ===========================

    @Test
    void getCart_성공_빈장바구니() {
        // when
        CartSummaryResponse result = cartService.getCart(testUser.getId());

        // then
        assertNotNull(result);
        assertNotNull(result.getCartProductList());
        assertTrue(result.getCartProductList().isEmpty());
        assertEquals(0L, result.getCartTotalPrice());
    }

    @Test
    void getCart_성공_상품있는장바구니() {
        // given
        Cart cart = testUser.getCart();
        CartProduct cartProduct = CartProduct.of(cart, testProduct, 2L);
        cartProductApiRepository.save(cartProduct);

        // when
        CartSummaryResponse result = cartService.getCart(testUser.getId());

        // then
        assertNotNull(result);
        assertEquals(1, result.getCartProductList().size());
        assertEquals(20000L, result.getCartTotalPrice()); // 10000 * 2
        assertEquals(2L, result.getCartProductList().get(0).getQuantity());
    }

    @Test
    void getCart_실패_존재하지않는유저() {
        // given
        Long nonExistentUserId = 99999L;

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> cartService.getCart(nonExistentUserId));
        assertEquals("User not found", exception.getMessage());
    }

    // =========================== addCartProduct 테스트 (중요도: 높음) ===========================

    @Test
    void addCartProduct_성공_새상품추가() {
        // given
        CartProductAddRequest request = new CartProductAddRequest(testProduct.getId(), 3L);

        // when
        CartSummaryResponse result = cartService.addCartProduct(testUser.getId(), request);

        // then
        assertNotNull(result);
        assertEquals(1, result.getCartProductList().size());
        assertEquals(3L, result.getCartProductList().get(0).getQuantity());
        assertEquals(30000L, result.getCartTotalPrice()); // 10000 * 3
    }

    @Test
    void addCartProduct_성공_기존상품수량증가() {
        // given
        Cart cart = testUser.getCart();
        CartProduct existingCartProduct = CartProduct.of(cart, testProduct, 2L);
        cartProductApiRepository.save(existingCartProduct);

        CartProductAddRequest request = new CartProductAddRequest(testProduct.getId(), 3L);

        // when
        CartSummaryResponse result = cartService.addCartProduct(testUser.getId(), request);

        // then
        assertNotNull(result);
        assertEquals(1, result.getCartProductList().size());
        assertEquals(5L, result.getCartProductList().get(0).getQuantity()); // 2 + 3
        assertEquals(50000L, result.getCartTotalPrice()); // 10000 * 5
    }

    @Test
    void addCartProduct_실패_존재하지않는상품() {
        // given
        CartProductAddRequest request = new CartProductAddRequest(99999L, 1L);

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> cartService.addCartProduct(testUser.getId(), request));
        assertEquals("Product not found", exception.getMessage());
    }

    @Test
    void addCartProduct_실패_잘못된수량() {
        // given
        CartProductAddRequest request = new CartProductAddRequest(testProduct.getId(), 0L);

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> cartService.addCartProduct(testUser.getId(), request));
        assertEquals("Quantity must be greater than 0", exception.getMessage());
    }

    // =========================== updateCartProductQuantity 테스트 (중요도: 높음) ===========================

    @Test
    void updateCartProductQuantity_성공_수량변경() {
        // given
        Cart cart = testUser.getCart();
        CartProduct cartProduct = CartProduct.of(cart, testProduct, 2L);
        cartProductApiRepository.save(cartProduct);

        CartProductQuantityUpdateRequest request = new CartProductQuantityUpdateRequest(5L);

        // when
        CartSummaryResponse result = cartService.updateCartProductQuantity(
                testUser.getId(), testProduct.getId(), request);

        // then
        assertNotNull(result);
        assertEquals(1, result.getCartProductList().size());
        assertEquals(5L, result.getCartProductList().get(0).getQuantity());
        assertEquals(50000L, result.getCartTotalPrice()); // 10000 * 5
    }

    @Test
    void updateCartProductQuantity_성공_수량0으로상품삭제() {
        // given
        Cart cart = testUser.getCart();
        CartProduct cartProduct = CartProduct.of(cart, testProduct, 2L);
        cartProductApiRepository.save(cartProduct);

        CartProductQuantityUpdateRequest request = new CartProductQuantityUpdateRequest(0L);

        // when
        CartSummaryResponse result = cartService.updateCartProductQuantity(
                testUser.getId(), testProduct.getId(), request);

        // then
        assertNotNull(result);
        assertTrue(result.getCartProductList().isEmpty());
        assertEquals(0L, result.getCartTotalPrice());

        // 데이터베이스에서도 삭제 확인
        Optional<CartProduct> deletedCartProduct = cartProductApiRepository
                .findByCart_IdAndProduct_Id(cart.getId(), testProduct.getId());
        assertFalse(deletedCartProduct.isPresent());
    }

    @Test
    void updateCartProductQuantity_실패_존재하지않는장바구니상품() {
        // given
        CartProductQuantityUpdateRequest request = new CartProductQuantityUpdateRequest(3L);

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> cartService.updateCartProductQuantity(testUser.getId(), testProduct.getId(), request));
        assertEquals("Cart product not found", exception.getMessage());
    }

    // =========================== deleteCartProduct 테스트 (중요도: 보통) ===========================

    @Test
    void deleteCartProduct_성공() {
        // given
        Cart cart = testUser.getCart();
        CartProduct cartProduct = CartProduct.of(cart, testProduct, 2L);
        cartProductApiRepository.save(cartProduct);

        // when
        CartSummaryResponse result = cartService.deleteCartProduct(testUser.getId(), testProduct.getId());

        // then
        assertNotNull(result);
        assertTrue(result.getCartProductList().isEmpty());
        assertEquals(0L, result.getCartTotalPrice());

        // 데이터베이스에서도 삭제 확인
        Optional<CartProduct> deletedCartProduct = cartProductApiRepository
                .findByCart_IdAndProduct_Id(cart.getId(), testProduct.getId());
        assertFalse(deletedCartProduct.isPresent());
    }

    @Test
    void deleteCartProduct_실패_존재하지않는장바구니상품() {
        // given & when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> cartService.deleteCartProduct(testUser.getId(), testProduct.getId()));
        assertEquals("Cart product not found", exception.getMessage());
    }
} 