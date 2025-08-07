package com.example.api.module.order.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.module.order.controller.request.OrderProductRequest;
import com.example.api.module.order.controller.response.OrderProductResponse;
import com.example.api.module.order.service.OrderService;
import com.example.core.model.response.DataResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/order")
public class OrderController {

    private final OrderService orderService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/product")
    public DataResponse<OrderProductResponse> orderProduct(
            @AuthenticationPrincipal Long userId,
            @RequestBody OrderProductRequest req) {
        return DataResponse.of(orderService.orderProduct(userId, req));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping()
    public DataResponse<List<OrderProductResponse>> getOrders(
            @AuthenticationPrincipal Long userId) {
        return DataResponse.of(orderService.getOrders(userId));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{orderId}")
    public DataResponse<OrderProductResponse> getOrder(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long orderId) {
        return DataResponse.of(orderService.getOrder(userId, orderId));
    }
}
