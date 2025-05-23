package com.example.api.module.order.service;

import com.example.api.module.cart.service.CartService;
import com.example.api.module.order.controller.request.OrderProductRequest;
import com.example.api.module.order.controller.response.OrderProductResponse;
import com.example.core.domain.cart.Cart;
import com.example.core.domain.cart_product.CartProduct;
import com.example.core.domain.cart_product.api.CartProductApiRepository;
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
import com.example.core.dto.OrderProductRequestDto;
import com.example.core.enums.OrderStatus;
import com.example.core.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartService cartService;

    private final ShippingAddressApiRepository shippingAddressApiRepository;
    private final ProductApiRepository productApiRepository;
    private final UserApiRepository userApiRepository;
    private final OrderApiRepository orderApiRepository;
    private final OrderProductApiRepository orderProductApiRepository;
    private final CartProductApiRepository cartProductApiRepository;


    @Transactional(readOnly = true)
    public OrderProductResponse getOrder(Long userId, Long orderId) {
        // 0. 유저 조회
        User user = userApiRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("user not found"));

        // 1. 주문 조회
        Order order = orderApiRepository.findById(orderId)
                .orElseThrow(() -> new BadRequestException("order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("not your order");
        }

        // 2. 주문 상품들 조회
        List<OrderProduct> orderProducts = order.getOrderProducts();

        return OrderProductResponse.of(userId, List.of(order), orderProducts);
    }

    @Transactional(readOnly = true)
    public List<OrderProductResponse> getOrders(Long userId) {
        User user = userApiRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("user not found"));

        List<OrderProductResponse> orderProductResponses = new ArrayList<>();
        for (Order order : user.getOrders()) {
            orderProductResponses.add(getOrder(userId, order.getId()));
        }

        return orderProductResponses;
    }

    // Order a single product
    @Transactional
    public OrderProductResponse orderProduct(Long userId, OrderProductRequest req) {
        OrderProductRequestDto reqDto = req.getOrderProducts().get(0);
        Long productId = reqDto.getProductId();

        // 0. 유저 조회
        User user = userApiRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("user not found"));
        // 1. 제품 조회
        Long quantity = reqDto.getQuantity();
        Product product = productApiRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new BadRequestException("product not found"));

        // 2. 제품 수량 확인
        if (product.getStockQuantity() < quantity) {
            throw new BadRequestException("not enough quantity");
        }

        // 3. 주문 배송지 조회
        Long shippingAddressId = req.getShippingAddressId();
        ShippingAddress shippingAddress = shippingAddressApiRepository.findByIdAndUserId(shippingAddressId, userId)
                .orElseThrow(() -> new BadRequestException("shipping address not found"));

        // 4. 제품 수량 차감
        product.setStockQuantity(product.getStockQuantity() - quantity);

        // 5. 주문 생성
        Order order = Order.createOrder(user, shippingAddress, null);
        orderApiRepository.save(order);
        // 6. 주문 상품들 생성
        OrderProduct orderProduct = OrderProduct.createOrderProduct(order, product, quantity, product.getPrice());
        orderProductApiRepository.save(orderProduct);

        return OrderProductResponse.of(userId, List.of(order), List.of(orderProduct));
    }

    // Order products from carts
    @Transactional
    public OrderProductResponse orderProductsFromCart(Long userId, OrderProductRequest req) {
        List<OrderProductRequestDto> reqDtos = req.getOrderProducts();
        Long shippingAddressId = req.getShippingAddressId();

        User user = userApiRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("user not found"));
        Cart cart = user.getCart();

        // 배송지 확인
        ShippingAddress shippingAddress = shippingAddressApiRepository.findByIdAndUserId(shippingAddressId, userId)
                .orElseThrow(() -> new BadRequestException("shipping address not found"));

        // 주문 생성
        Order order = Order.createOrder(user, shippingAddress, null);
        List<OrderProduct> orderProducts = new ArrayList<>();
        for (OrderProductRequestDto reqDto : reqDtos) {
            Long productId = reqDto.getProductId();

            // 제품 조회
            CartProduct cartProduct = cartProductApiRepository.findByCart_IdAndProduct_Id(cart.getId(), productId)
                    .orElseThrow(() -> new BadRequestException("cart product not found"));
            Long cartProductQuantity = cartProduct.getQuantity();

            Product product = productApiRepository.findByIdAndIsDeletedFalse(productId)
                    .orElseThrow(() -> new BadRequestException("product not found"));
            // 제품 수량 확인
            if (product.getStockQuantity() < cartProductQuantity) {
                throw new BadRequestException("not enough quantity");
            }

            // 제품 수량 차감
            product.setStockQuantity(product.getStockQuantity() - cartProductQuantity);
            cartService.deleteCartProduct(cart.getId(), productId);

            // 주문 상품 생성
            OrderProduct orderProduct = OrderProduct.createOrderProduct(order, product, cartProductQuantity, product.getPrice());
            orderProducts.add(orderProduct);
        }

        orderApiRepository.save(order);
        orderProductApiRepository.saveAll(orderProducts);
        
        return OrderProductResponse.of(userId, List.of(order), orderProducts);
    }




}
