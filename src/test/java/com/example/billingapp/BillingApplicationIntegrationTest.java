package com.example.billingapp;

import com.example.billingapp.model.Customer;
import com.example.billingapp.model.Invoice;
import com.example.billingapp.model.Payment;
import com.example.billingapp.repository.CustomerRepository;
import com.example.billingapp.repository.InvoiceRepository;
import com.example.billingapp.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BillingApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private Customer customer;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        invoiceRepository.deleteAll();
        customerRepository.deleteAll();

        customer = new Customer();
        customer.setName("John Doe");
        customer.setEmail("john@example.com");
        customer.setPhone("123-456-7890");
        customer = customerRepository.save(customer);
    }

    @Test
    void createCustomerAndInvoiceAndPayment_EndToEnd() throws Exception {
        // Create Invoice
        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setAmount(1000.0);
        invoice.setDueDate(LocalDate.now().plusDays(30));

        String invoiceJson = mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invoice)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.amount").value(1000.0))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Invoice createdInvoice = objectMapper.readValue(invoiceJson, Invoice.class);
        Long invoiceId = createdInvoice.getId();

        // Create Payment
        Payment payment = new Payment();
        payment.setInvoice(createdInvoice);
        payment.setAmount(500.0);
        payment.setPaymentDate(LocalDate.now());
        payment.setTransactionNumber("TXN-" + System.currentTimeMillis());

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.amount").value(500.0));

        // Verify Dashboard Summary
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCustomers").value(1))
                .andExpect(jsonPath("$.totalInvoices").value(1))
                .andExpect(jsonPath("$.totalAmountInvoiced").value(1000.0))
                .andExpect(jsonPath("$.totalAmountPaid").value(500.0))
                .andExpect(jsonPath("$.outstandingBalance").value(500.0));
    }

    @Test
    void createPaymentExceedingInvoiceAmount_ReturnsBadRequest() throws Exception {
        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setAmount(1000.0);
        invoice.setDueDate(LocalDate.now().plusDays(30));
        Invoice savedInvoice = invoiceRepository.save(invoice);

        Payment payment = new Payment();
        payment.setInvoice(savedInvoice);
        payment.setAmount(1500.0); // Exceeds invoice amount
        payment.setPaymentDate(LocalDate.now());
        payment.setTransactionNumber("TXN-EXCEED");

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payment)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteInvoiceWithPayments_ReturnsBadRequest() throws Exception {
        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setAmount(1000.0);
        invoice.setDueDate(LocalDate.now().plusDays(30));
        Invoice savedInvoice = invoiceRepository.save(invoice);

        Payment payment = new Payment();
        payment.setInvoice(savedInvoice);
        payment.setAmount(500.0);
        payment.setPaymentDate(LocalDate.now());
        payment.setTransactionNumber("TXN-DELETE");
        paymentRepository.save(payment);

        mockMvc.perform(delete("/api/invoices/" + savedInvoice.getId()))
                .andExpect(status().isBadRequest());
    }
}
