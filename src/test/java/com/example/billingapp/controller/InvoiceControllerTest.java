package com.example.billingapp.controller;

import com.example.billingapp.model.Customer;
import com.example.billingapp.model.Invoice;
import com.example.billingapp.model.OverdueInvoiceDTO;
import com.example.billingapp.service.InvoiceService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InvoiceController.class)
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvoiceService invoiceService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createInvoice_Success() throws Exception {
        Customer customer = new Customer();
        customer.setId(1L);

        Invoice invoice = new Invoice();
        invoice.setId(1L);
        invoice.setCustomer(customer);
        invoice.setAmount(1000.0);
        invoice.setDueDate(LocalDate.now().plusDays(30));

        when(invoiceService.createInvoice(any(Invoice.class))).thenReturn(invoice);

        mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invoice)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.amount").value(1000.0));

        verify(invoiceService).createInvoice(any(Invoice.class));
    }

    @Test
    void getAllInvoices_Success() throws Exception {
        Invoice invoice1 = new Invoice();
        invoice1.setId(1L);
        invoice1.setAmount(1000.0);

        Invoice invoice2 = new Invoice();
        invoice2.setId(2L);
        invoice2.setAmount(2000.0);

        List<Invoice> invoices = Arrays.asList(invoice1, invoice2);
        when(invoiceService.getAllInvoices()).thenReturn(invoices);

        mockMvc.perform(get("/api/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(invoiceService).getAllInvoices();
    }

    @Test
    void getInvoiceById_Success() throws Exception {
        Invoice invoice = new Invoice();
        invoice.setId(1L);
        invoice.setAmount(1000.0);

        when(invoiceService.getInvoiceById(1L)).thenReturn(invoice);

        mockMvc.perform(get("/api/invoices/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(invoiceService).getInvoiceById(1L);
    }

    @Test
    void deleteInvoice_Success() throws Exception {
        doNothing().when(invoiceService).deleteInvoice(1L);

        mockMvc.perform(delete("/api/invoices/1"))
                .andExpect(status().isNoContent());

        verify(invoiceService).deleteInvoice(1L);
    }

    @Test
    void getOverdueInvoices_Success() throws Exception {
        OverdueInvoiceDTO dto = new OverdueInvoiceDTO();
        dto.setInvoiceNumber("INV1");
        dto.setCustomerName("John Doe");
        dto.setAmount(1000.0);

        List<OverdueInvoiceDTO> overdueInvoices = Arrays.asList(dto);
        when(invoiceService.getOverdueInvoices(any(), any(), any())).thenReturn(overdueInvoices);

        mockMvc.perform(get("/api/invoices/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(invoiceService).getOverdueInvoices(null, null, null);
    }

    @Test
    void getOverdueInvoices_WithFilters_Success() throws Exception {
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();

        List<OverdueInvoiceDTO> overdueInvoices = Arrays.asList();
        when(invoiceService.getOverdueInvoices(eq(1L), eq(startDate), eq(endDate)))
                .thenReturn(overdueInvoices);

        mockMvc.perform(get("/api/invoices/overdue")
                        .param("customerId", "1")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk());

        verify(invoiceService).getOverdueInvoices(eq(1L), eq(startDate), eq(endDate));
    }
}
