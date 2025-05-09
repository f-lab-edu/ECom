package com.example.api.module.cart.controller.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartProductQuantityUpdateRequest {
    private Long quantity;
}
