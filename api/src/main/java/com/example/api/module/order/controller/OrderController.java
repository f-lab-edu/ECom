package com.example.api.module.order.controller;

import com.example.api.module.order.controller.request.OrderProductRequest;
import com.example.api.module.order.controller.response.OrderProductResponse;
import com.example.api.module.order.service.OrderService;
import com.example.core.model.response.DataResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/order")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/product")
    public DataResponse<OrderProductResponse> orderProduct(
            @AuthenticationPrincipal Long userId,
            @RequestBody OrderProductRequest req) {
        return DataResponse.of(orderService.orderProduct(userId, req));
    }

    @GetMapping()
    public DataResponse<List<OrderProductResponse>> getOrders(
            @AuthenticationPrincipal Long userId) {
        return DataResponse.of(orderService.getOrders(userId));
    }

    @GetMapping("/{orderId}")
    public DataResponse<OrderProductResponse> getOrder(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long orderId) {
        return DataResponse.of(orderService.getOrder(userId, orderId));
    }
}
