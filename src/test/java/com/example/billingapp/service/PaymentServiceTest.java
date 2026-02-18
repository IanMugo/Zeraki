package com.example.billingapp.service;

import com.example.billingapp.exception.BusinessRuleViolationException;
import com.example.billingapp.exception.ResourceNotFoundException;
import com.example.billingapp.model.Invoice;
import com.example.billingapp.model.Payment;
import com.example.billingapp.repository.InvoiceRepository;
import com.example.billingapp.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Invoice invoice;
    private Payment payment;

    @BeforeEach
    void setUp() {
        invoice = new Invoice();
        invoice.setId(1L);
        invoice.setAmount(1000.0);
        invoice.setPayments(new ArrayList<>());

        payment = new Payment();
        payment.setId(1L);
        payment.setInvoice(invoice);
        payment.setAmount(500.0);
        payment.setPaymentDate(LocalDate.now());
        payment.setTransactionNumber("TXN-001");
    }

    @Test
    void createPayment_Success() {
        when(invoiceRepository.existsById(1L)).thenReturn(true);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(paymentRepository.existsByTransactionNumber("TXN-001")).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        Payment result = paymentService.createPayment(payment);

        assertNotNull(result);
        assertEquals(500.0, result.getAmount());
        verify(invoiceRepository).existsById(1L);
        verify(paymentRepository).save(payment);
    }

    @Test
    void createPayment_InvalidInvoice_ThrowsException() {
        payment.setInvoice(null);

        assertThrows(BusinessRuleViolationException.class, () -> {
            paymentService.createPayment(payment);
        });

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createPayment_InvoiceNotExists_ThrowsException() {
        when(invoiceRepository.existsById(1L)).thenReturn(false);

        assertThrows(BusinessRuleViolationException.class, () -> {
            paymentService.createPayment(payment);
        });

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createPayment_InvalidAmount_ThrowsException() {
        payment.setAmount(0.0);
        when(invoiceRepository.existsById(1L)).thenReturn(true);

        assertThrows(BusinessRuleViolationException.class, () -> {
            paymentService.createPayment(payment);
        });

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createPayment_FutureDate_ThrowsException() {
        payment.setPaymentDate(LocalDate.now().plusDays(1));
        when(invoiceRepository.existsById(1L)).thenReturn(true);

        assertThrows(BusinessRuleViolationException.class, () -> {
            paymentService.createPayment(payment);
        });

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createPayment_MissingTransactionNumber_ThrowsException() {
        payment.setTransactionNumber(null);
        when(invoiceRepository.existsById(1L)).thenReturn(true);

        assertThrows(BusinessRuleViolationException.class, () -> {
            paymentService.createPayment(payment);
        });

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createPayment_DuplicateTransactionNumber_ThrowsException() {
        when(invoiceRepository.existsById(1L)).thenReturn(true);
        when(paymentRepository.existsByTransactionNumber("TXN-001")).thenReturn(true);

        assertThrows(BusinessRuleViolationException.class, () -> {
            paymentService.createPayment(payment);
        });

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createPayment_ExceedsInvoiceAmount_ThrowsException() {
        payment.setAmount(1500.0);
        when(invoiceRepository.existsById(1L)).thenReturn(true);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(paymentRepository.existsByTransactionNumber("TXN-001")).thenReturn(false);

        assertThrows(BusinessRuleViolationException.class, () -> {
            paymentService.createPayment(payment);
        });

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createPayment_TotalExceedsInvoiceAmount_ThrowsException() {
        Payment existingPayment = new Payment();
        existingPayment.setAmount(600.0);
        invoice.getPayments().add(existingPayment);

        payment.setAmount(500.0);
        when(invoiceRepository.existsById(1L)).thenReturn(true);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(paymentRepository.existsByTransactionNumber("TXN-001")).thenReturn(false);

        assertThrows(BusinessRuleViolationException.class, () -> {
            paymentService.createPayment(payment);
        });

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void getAllPayments_Success() {
        List<Payment> payments = Arrays.asList(payment);
        when(paymentRepository.findAll()).thenReturn(payments);

        List<Payment> result = paymentService.getAllPayments();

        assertEquals(1, result.size());
        verify(paymentRepository).findAll();
    }

    @Test
    void getPaymentById_Success() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        Payment result = paymentService.getPaymentById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(paymentRepository).findById(1L);
    }

    @Test
    void getPaymentById_NotFound_ThrowsException() {
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            paymentService.getPaymentById(999L);
        });
    }
}
