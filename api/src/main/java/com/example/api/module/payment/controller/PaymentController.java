package com.example.api.module.payment.controller;

import com.example.api.module.payment.controller.request.PaymentRequest;
import com.example.api.module.payment.controller.response.PaymentResponse;
import com.example.api.module.payment.service.PaymentService;
import com.example.core.model.response.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payment")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping()
    public DataResponse<PaymentResponse> payment(
            @AuthenticationPrincipal Long userId,
            @RequestBody PaymentRequest req) {
        return DataResponse.of(paymentService.makePayment(userId, req));
    }
}
