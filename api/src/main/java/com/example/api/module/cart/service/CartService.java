package com.example.api.module.cart.service;

import com.example.api.module.cart.controller.request.CartProductAddRequest;
import com.example.api.module.cart.controller.request.CartProductQuantityUpdateRequest;
import com.example.api.module.cart.controller.response.CartSummaryResponse;
import com.example.core.domain.cart.Cart;
import com.example.core.domain.cart_product.CartProduct;
import com.example.core.domain.cart_product.api.CartProductApiRepository;
import com.example.core.domain.product.Product;
import com.example.core.domain.product.api.ProductApiRepository;
import com.example.core.domain.user.User;
import com.example.core.domain.user.api.UserApiRepository;
import com.example.core.dto.CartProductDto;
import com.example.core.dto.ProductSummaryDto;
import com.example.core.enums.CartProductStatus;
import com.example.core.exception.BadRequestException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CartService {

    private final UserApiRepository userApiRepository;
    private final ProductApiRepository productApiRepository;
    private final CartProductApiRepository cartProductApiRepository;

    @Transactional(readOnly = true)
    public CartSummaryResponse getCart(Long userId) {
        User user = userApiRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        return getCartSummary(user);
    }

    @Transactional
    public CartSummaryResponse addCartProduct(Long userId, CartProductAddRequest req) {
        User user = userApiRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        Cart cart = user.getCart();
        Product product = productApiRepository.findById(req.getProductId())
                .orElseThrow(() -> new BadRequestException("Product not found"));

        Long quantity = req.getAddQuantity();
        if (quantity <= 0) throw new BadRequestException("Quantity must be greater than 0");

        Optional<CartProduct> cartProductOpt = cartProductApiRepository.
                findByCart_IdAndProduct_Id(cart.getId(), req.getProductId());

        CartProduct cartProduct;
        if (cartProductOpt.isPresent()) {
            //update existing cart product
            cartProduct = cartProductOpt.get();
            cartProduct.addQuantity(quantity);
        } else {
            //create new cart product
            cartProduct = CartProduct.of(cart, product, quantity);
        }
        cartProductApiRepository.save(cartProduct);
        return getCartSummary(user);
    }

    @Transactional
    public CartSummaryResponse updateCartProductQuantity(Long userId, Long productId, CartProductQuantityUpdateRequest req) {
        User user = userApiRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        Cart cart = user.getCart();

        CartProduct cartProduct = cartProductApiRepository.findByCart_IdAndProduct_Id(cart.getId(), productId)
                .orElseThrow(() -> new BadRequestException("Cart product not found"));

        Long quantity = req.getQuantity();

        if (quantity <= 0) {
            cartProductApiRepository.delete(cartProduct);
        } else {
            cartProduct.setQuantity(quantity);
            cartProductApiRepository.save(cartProduct);
        }
        return getCartSummary(user);
    }

    @Transactional
    public CartSummaryResponse deleteCartProduct(Long userId, Long productId) {
        User user = userApiRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        Cart cart = user.getCart();

        CartProduct cartProduct = cartProductApiRepository.findByCart_IdAndProduct_Id(cart.getId(), productId)
                .orElseThrow(() -> new BadRequestException("Cart product not found"));

        cartProductApiRepository.delete(cartProduct);
        return getCartSummary(user);
    }


    private CartSummaryResponse getCartSummary(User user) {
        Cart cart = user.getCart();

        List<CartProduct> cartProductList = cartProductApiRepository.findByCartId(cart.getId());

        List<CartProductDto> cartProductDtoList = new ArrayList<>();

        for (CartProduct cartProduct : cartProductList) {
            Product product = cartProduct.getProduct();
            ProductSummaryDto productSummaryDto;
            Long quantity = 0L;
            CartProductStatus status;

            if (product == null || product.isDeleted()) {
                status = CartProductStatus.DELETED;
                productSummaryDto = null;

            } else if (product.getStockQuantity() < cartProduct.getQuantity()) {
                status = CartProductStatus.SHORTAGE;
                productSummaryDto = ProductSummaryDto.of(product);
                quantity = product.getStockQuantity();

            } else {
                status = CartProductStatus.AVAILABLE;
                productSummaryDto = ProductSummaryDto.of(product);
                quantity = cartProduct.getQuantity();
            }
            Long totalPrice = (productSummaryDto != null) ? productSummaryDto.getPrice() * cartProduct.getQuantity() : 0;

            cartProductDtoList.add(new CartProductDto(
                    productSummaryDto,
                    quantity,
                    status,
                    totalPrice
            ));
        }
        return CartSummaryResponse.of(cartProductDtoList);
    }
}
