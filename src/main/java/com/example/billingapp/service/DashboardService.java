package com.example.billingapp.service;

import com.example.billingapp.model.Customer;
import com.example.billingapp.model.Invoice;
import com.example.billingapp.model.Payment;
import com.example.billingapp.repository.CustomerRepository;
import com.example.billingapp.repository.InvoiceRepository;
import com.example.billingapp.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class DashboardService {

    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    public DashboardService(CustomerRepository customerRepository, InvoiceRepository invoiceRepository, PaymentRepository paymentRepository) {
        this.customerRepository = customerRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
    }

    public Map<String, Object> getSummary(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> summary = new HashMap<>();

        List<Customer> customers = customerRepository.findAll();
        long totalCustomers = customers.stream()
                .filter(c -> filterByDate(c.getCreatedAt().toLocalDate(), startDate, endDate))
                .count();

        List<Invoice> invoices = invoiceRepository.findAll();
        long totalInvoices = invoices.stream()
                .filter(i -> filterByDate(i.getCreatedAt().toLocalDate(), startDate, endDate))
                .count();

        double totalAmountInvoiced = invoices.stream()
                .filter(i -> filterByDate(i.getCreatedAt().toLocalDate(), startDate, endDate))
                .mapToDouble(Invoice::getAmount)
                .sum();

        double totalAmountPaid = paymentRepository.findAll().stream()
                .filter(p -> filterByDate(p.getPaymentDate(), startDate, endDate))
                .mapToDouble(Payment::getAmount)
                .sum();

        double outstandingBalance = totalAmountInvoiced - totalAmountPaid;

        summary.put("totalCustomers", totalCustomers);
        summary.put("totalInvoices", totalInvoices);
        summary.put("totalAmountInvoiced", totalAmountInvoiced);
        summary.put("totalAmountPaid", totalAmountPaid);
        summary.put("outstandingBalance", outstandingBalance);

        return summary;
    }

    public List<Map<String, Object>> getTopCustomers(LocalDate startDate, LocalDate endDate) {
        Map<Customer, Double> customerPayments = new HashMap<>();
        for (Payment payment : paymentRepository.findAll()) {
            if (filterByDate(payment.getPaymentDate(), startDate, endDate)) {
                Customer customer = payment.getInvoice().getCustomer();
                customerPayments.put(customer, customerPayments.getOrDefault(customer, 0.0) + payment.getAmount());
            }
        }
        return customerPayments.entrySet().stream()
                .sorted(Comparator.<Map.Entry<Customer, Double>>comparingDouble(Map.Entry::getValue).reversed())
                .limit(5)
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("customerName", entry.getKey().getName());
                    map.put("totalPaid", entry.getValue());
                    return map;
                })
                .toList();
    }

    public List<Map<String, Object>> getMonthlyRevenue(LocalDate startDate, LocalDate endDate) {
        Map<String, Double> monthlyTotals = new TreeMap<>();
        for (Payment payment : paymentRepository.findAll()) {
            LocalDate date = payment.getPaymentDate();
            if (filterByDate(date, startDate, endDate)) {
                String monthKey = date.getYear() + "-" + String.format("%02d", date.getMonthValue());
                monthlyTotals.put(monthKey, monthlyTotals.getOrDefault(monthKey, 0.0) + payment.getAmount());
            }
        }
        return monthlyTotals.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("month", entry.getKey());
                    map.put("total", entry.getValue());
                    return map;
                })
                .toList();
    }

    private boolean filterByDate(LocalDate date, LocalDate start, LocalDate end) {
        if (start != null && date.isBefore(start)) return false;
        if (end != null && date.isAfter(end)) return false;
        return true;
    }
}
