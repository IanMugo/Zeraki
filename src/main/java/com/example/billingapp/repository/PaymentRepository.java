package com.example.billingapp.repository;

import com.example.billingapp.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    boolean existsByTransactionNumber(String transactionNumber);
}
