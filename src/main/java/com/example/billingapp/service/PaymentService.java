package com.example.billingapp.service;

import com.example.billingapp.exception.BusinessRuleViolationException;
import com.example.billingapp.exception.ResourceNotFoundException;
import com.example.billingapp.model.Payment;
import com.example.billingapp.repository.InvoiceRepository;
import com.example.billingapp.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    public PaymentService(PaymentRepository paymentRepository, InvoiceRepository invoiceRepository) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public Payment createPayment(Payment payment) {
        if (payment.getInvoice() == null
                || payment.getInvoice().getId() == null
                || !invoiceRepository.existsById(payment.getInvoice().getId())) {
            throw new BusinessRuleViolationException("Invalid invoice");
        }
        if (payment.getAmount() == null || payment.getAmount() <= 0) {
            throw new BusinessRuleViolationException("Payment amount must be positive");
        }
        if (payment.getPaymentDate() == null) {
            throw new BusinessRuleViolationException("Payment date is required");
        }
        if (payment.getPaymentDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleViolationException("A payment's date must be on or before the current date");
        }
        if (payment.getTransactionNumber() == null || payment.getTransactionNumber().isBlank()) {
            throw new BusinessRuleViolationException("Transaction number is required");
        }
        if (paymentRepository.existsByTransactionNumber(payment.getTransactionNumber())) {
            throw new BusinessRuleViolationException("Transaction number must be unique");
        }

        var invoice = invoiceRepository.findById(payment.getInvoice().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        double totalPaid = invoice.getPayments().stream().mapToDouble(Payment::getAmount).sum() + payment.getAmount();
        if (totalPaid > invoice.getAmount()) {
            throw new BusinessRuleViolationException("A payment's total + previous payments must not exceed the invoice amount");
        }

        payment.setInvoice(invoice);
        return paymentRepository.save(payment);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }
}
