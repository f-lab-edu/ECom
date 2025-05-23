package com.example.api.module.payment.externalPayment.config;

import com.example.api.module.payment.externalPayment.service.ExternalPaymentService;
import com.example.api.module.payment.externalPayment.service.impl.DummyExternalPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ExternalPaymentConfig {

    private final DummyExternalPaymentService dummyExternalPaymentService;

    @Bean
    public ExternalPaymentService externalPaymentService() {
        return dummyExternalPaymentService;
    }
}
