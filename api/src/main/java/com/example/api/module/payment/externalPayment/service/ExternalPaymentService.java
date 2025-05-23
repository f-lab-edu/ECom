package com.example.api.module.payment.externalPayment.service;

import com.example.core.enums.PaymentMehtod;

public interface ExternalPaymentService {

    public boolean sendPaymentRequestIsSuccess(PaymentMehtod paymentMethod, Long paymentAmount);
}
