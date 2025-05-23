package com.example.core.domain.payment.api;

import com.example.core.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentApiRepository extends JpaRepository<Payment, Long> {

}
