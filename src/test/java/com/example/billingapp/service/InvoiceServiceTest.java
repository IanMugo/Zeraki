package com.example.billingapp.service;

import com.example.billingapp.exception.BusinessRuleViolationException;
import com.example.billingapp.exception.ResourceNotFoundException;
import com.example.billingapp.model.Customer;
import com.example.billingapp.model.Invoice;
import com.example.billingapp.model.Payment;
import com.example.billingapp.model.enums.InvoiceStatus;
import com.example.billingapp.repository.CustomerRepository;
import com.example.billingapp.repository.InvoiceRepository;
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
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    private Customer customer;
    private Invoice invoice;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");
        customer.setEmail("john@example.com");

        invoice = new Invoice();
        invoice.setId(1L);
        invoice.setCustomer(customer);
        invoice.setAmount(1000.0);
        invoice.setDueDate(LocalDate.now().plusDays(30));
        invoice.setPayments(new ArrayList<>());
    }

    @Test
    void createInvoice_Success() {
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

        Invoice result = invoiceService.createInvoice(invoice);

        assertNotNull(result);
        assertEquals(1000.0, result.getAmount());
        verify(customerRepository).existsById(1L);
        verify(invoiceRepository).save(invoice);
    }

    @Test
    void createInvoice_InvalidCustomer_ThrowsException() {
        invoice.setCustomer(null);

        assertThrows(BusinessRuleViolationException.class, () -> {
            invoiceService.createInvoice(invoice);
        });

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void createInvoice_CustomerNotExists_ThrowsException() {
        when(customerRepository.existsById(1L)).thenReturn(false);

        assertThrows(BusinessRuleViolationException.class, () -> {
            invoiceService.createInvoice(invoice);
        });

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void createInvoice_InvalidAmount_ThrowsException() {
        invoice.setAmount(0.0);
        when(customerRepository.existsById(1L)).thenReturn(true);

        assertThrows(BusinessRuleViolationException.class, () -> {
            invoiceService.createInvoice(invoice);
        });

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void createInvoice_DueDateNotInFuture_ThrowsException() {
        invoice.setDueDate(LocalDate.now());
        when(customerRepository.existsById(1L)).thenReturn(true);

        assertThrows(BusinessRuleViolationException.class, () -> {
            invoiceService.createInvoice(invoice);
        });

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void getInvoiceById_Success() {
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        Invoice result = invoiceService.getInvoiceById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(invoiceRepository).findById(1L);
    }

    @Test
    void getInvoiceById_NotFound_ThrowsException() {
        when(invoiceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            invoiceService.getInvoiceById(999L);
        });
    }

    @Test
    void deleteInvoice_Success() {
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        doNothing().when(invoiceRepository).delete(invoice);

        invoiceService.deleteInvoice(1L);

        verify(invoiceRepository).findById(1L);
        verify(invoiceRepository).delete(invoice);
    }

    @Test
    void deleteInvoice_WithPayments_ThrowsException() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setAmount(100.0);
        invoice.getPayments().add(payment);

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        assertThrows(BusinessRuleViolationException.class, () -> {
            invoiceService.deleteInvoice(1L);
        });

        verify(invoiceRepository, never()).delete(any());
    }

    @Test
    void getOverdueInvoices_Success() {
        Invoice overdueInvoice = new Invoice();
        overdueInvoice.setId(2L);
        overdueInvoice.setCustomer(customer);
        overdueInvoice.setAmount(500.0);
        overdueInvoice.setDueDate(LocalDate.now().minusDays(10));
        overdueInvoice.setPayments(new ArrayList<>());

        List<Invoice> invoices = Arrays.asList(invoice, overdueInvoice);
        when(invoiceRepository.findAll()).thenReturn(invoices);

        List<?> result = invoiceService.getOverdueInvoices(null, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
