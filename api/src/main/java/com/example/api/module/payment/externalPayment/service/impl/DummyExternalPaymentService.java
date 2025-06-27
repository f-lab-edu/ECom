package com.example.api.module.payment.externalPayment.service.impl;

import com.example.api.module.payment.externalPayment.service.ExternalPaymentService;
import com.example.core.dto.PaymentRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DummyExternalPaymentService implements ExternalPaymentService {


    @Override
    public String sendPaymentRequestIsSuccess(PaymentRequestDto dto) {
        return "RANDOM_TRANSACTION_ID";
    }
}
