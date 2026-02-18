package com.example.billingapp.repository;

import com.example.billingapp.model.Customer;
import com.example.billingapp.model.Invoice;
import com.example.billingapp.model.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PaymentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer;
    private Invoice invoice;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setName("John Doe");
        customer.setEmail("john@example.com");
        customer = entityManager.persistAndFlush(customer);

        invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setAmount(1000.0);
        invoice.setDueDate(LocalDate.now().plusDays(30));
        invoice = entityManager.persistAndFlush(invoice);
    }

    @Test
    void savePayment_Success() {
        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmount(500.0);
        payment.setPaymentDate(LocalDate.now());
        payment.setTransactionNumber("TXN-001");

        Payment saved = paymentRepository.save(payment);

        assertNotNull(saved.getId());
        assertEquals(500.0, saved.getAmount());
        assertEquals("TXN-001", saved.getTransactionNumber());
    }

    @Test
    void existsByTransactionNumber_True() {
        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmount(500.0);
        payment.setPaymentDate(LocalDate.now());
        payment.setTransactionNumber("TXN-001");
        entityManager.persistAndFlush(payment);

        boolean exists = paymentRepository.existsByTransactionNumber("TXN-001");

        assertTrue(exists);
    }

    @Test
    void existsByTransactionNumber_False() {
        boolean exists = paymentRepository.existsByTransactionNumber("TXN-999");

        assertFalse(exists);
    }

    @Test
    void findById_Success() {
        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmount(500.0);
        payment.setPaymentDate(LocalDate.now());
        payment.setTransactionNumber("TXN-001");
        Payment saved = entityManager.persistAndFlush(payment);

        Payment found = paymentRepository.findById(saved.getId()).orElse(null);

        assertNotNull(found);
        assertEquals(500.0, found.getAmount());
    }
}
