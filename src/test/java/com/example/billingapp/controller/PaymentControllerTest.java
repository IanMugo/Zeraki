package com.example.billingapp.controller;

import com.example.billingapp.model.Invoice;
import com.example.billingapp.model.Payment;
import com.example.billingapp.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createPayment_Success() throws Exception {
        Invoice invoice = new Invoice();
        invoice.setId(1L);

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setInvoice(invoice);
        payment.setAmount(500.0);
        payment.setPaymentDate(LocalDate.now());
        payment.setTransactionNumber("TXN-001");

        when(paymentService.createPayment(any(Payment.class))).thenReturn(payment);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.amount").value(500.0))
                .andExpect(jsonPath("$.transactionNumber").value("TXN-001"));

        verify(paymentService).createPayment(any(Payment.class));
    }

    @Test
    void getAllPayments_Success() throws Exception {
        Payment payment1 = new Payment();
        payment1.setId(1L);
        payment1.setAmount(500.0);

        Payment payment2 = new Payment();
        payment2.setId(2L);
        payment2.setAmount(300.0);

        List<Payment> payments = Arrays.asList(payment1, payment2);
        when(paymentService.getAllPayments()).thenReturn(payments);

        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(paymentService).getAllPayments();
    }

    @Test
    void getPaymentById_Success() throws Exception {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setAmount(500.0);

        when(paymentService.getPaymentById(1L)).thenReturn(payment);

        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.amount").value(500.0));

        verify(paymentService).getPaymentById(1L);
    }
}
