package com.example.billingapp.controller;

import com.example.billingapp.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @Test
    void getSummary_Success() throws Exception {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCustomers", 5L);
        summary.put("totalInvoices", 10L);
        summary.put("totalAmountInvoiced", 50000.0);
        summary.put("totalAmountPaid", 30000.0);
        summary.put("outstandingBalance", 20000.0);

        when(dashboardService.getSummary(any(), any())).thenReturn(summary);

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCustomers").value(5))
                .andExpect(jsonPath("$.totalInvoices").value(10))
                .andExpect(jsonPath("$.outstandingBalance").value(20000.0));

        verify(dashboardService).getSummary(null, null);
    }

    @Test
    void getSummary_WithDateRange_Success() throws Exception {
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCustomers", 2L);

        when(dashboardService.getSummary(eq(startDate), eq(endDate))).thenReturn(summary);

        mockMvc.perform(get("/api/dashboard/summary")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk());

        verify(dashboardService).getSummary(eq(startDate), eq(endDate));
    }

    @Test
    void getTopCustomers_Success() throws Exception {
        List<Map<String, Object>> topCustomers = new ArrayList<>();
        Map<String, Object> customer1 = new HashMap<>();
        customer1.put("customerName", "John Doe");
        customer1.put("totalPaid", 5000.0);
        topCustomers.add(customer1);

        when(dashboardService.getTopCustomers(any(), any())).thenReturn(topCustomers);

        mockMvc.perform(get("/api/dashboard/top-customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].customerName").value("John Doe"));

        verify(dashboardService).getTopCustomers(null, null);
    }

    @Test
    void getMonthlyRevenue_Success() throws Exception {
        List<Map<String, Object>> monthlyRevenue = new ArrayList<>();
        Map<String, Object> month1 = new HashMap<>();
        month1.put("month", "2026-02");
        month1.put("total", 10000.0);
        monthlyRevenue.add(month1);

        when(dashboardService.getMonthlyRevenue(any(), any())).thenReturn(monthlyRevenue);

        mockMvc.perform(get("/api/dashboard/monthly-revenue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].month").value("2026-02"));

        verify(dashboardService).getMonthlyRevenue(null, null);
    }
}
