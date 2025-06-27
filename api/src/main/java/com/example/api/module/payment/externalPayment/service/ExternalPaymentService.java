package com.example.api.module.payment.externalPayment.service;

import com.example.core.dto.PaymentRequestDto;

public interface ExternalPaymentService {

    public String sendPaymentRequestIsSuccess(PaymentRequestDto dto);
}
