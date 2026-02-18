package com.example.billingapp.repository;

import com.example.billingapp.model.Customer;
import com.example.billingapp.model.Invoice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class InvoiceRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setName("John Doe");
        customer.setEmail("john@example.com");
        customer = entityManager.persistAndFlush(customer);
    }

    @Test
    void saveInvoice_Success() {
        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setAmount(1000.0);
        invoice.setDueDate(LocalDate.now().plusDays(30));

        Invoice saved = invoiceRepository.save(invoice);

        assertNotNull(saved.getId());
        assertEquals(1000.0, saved.getAmount());
        assertEquals(customer.getId(), saved.getCustomer().getId());
    }

    @Test
    void findById_Success() {
        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setAmount(1000.0);
        invoice.setDueDate(LocalDate.now().plusDays(30));
        Invoice saved = entityManager.persistAndFlush(invoice);

        Invoice found = invoiceRepository.findById(saved.getId()).orElse(null);

        assertNotNull(found);
        assertEquals(1000.0, found.getAmount());
    }

    @Test
    void findAll_Success() {
        Invoice invoice1 = new Invoice();
        invoice1.setCustomer(customer);
        invoice1.setAmount(1000.0);
        invoice1.setDueDate(LocalDate.now().plusDays(30));

        Invoice invoice2 = new Invoice();
        invoice2.setCustomer(customer);
        invoice2.setAmount(2000.0);
        invoice2.setDueDate(LocalDate.now().plusDays(60));

        entityManager.persistAndFlush(invoice1);
        entityManager.persistAndFlush(invoice2);

        List<Invoice> invoices = invoiceRepository.findAll();
        assertEquals(2, invoices.size());
    }

    @Test
    void deleteInvoice_Success() {
        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setAmount(1000.0);
        invoice.setDueDate(LocalDate.now().plusDays(30));
        Invoice saved = entityManager.persistAndFlush(invoice);

        invoiceRepository.delete(saved);
        entityManager.flush();

        assertFalse(invoiceRepository.findById(saved.getId()).isPresent());
    }
}
