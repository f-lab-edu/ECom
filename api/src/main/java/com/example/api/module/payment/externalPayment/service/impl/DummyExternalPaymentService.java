package com.example.api.module.payment.externalPayment.service.impl;

import com.example.api.module.payment.externalPayment.service.ExternalPaymentService;
import com.example.core.enums.PaymentMehtod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DummyExternalPaymentService implements ExternalPaymentService {


    @Override
    public boolean sendPaymentRequestIsSuccess(PaymentMehtod paymentMethod, Long paymentAmount) {
        return true;
    }
}
