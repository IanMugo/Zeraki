package com.example.billingapp.service;

import com.example.billingapp.exception.BusinessRuleViolationException;
import com.example.billingapp.exception.ResourceNotFoundException;
import com.example.billingapp.model.Invoice;
import com.example.billingapp.model.OverdueInvoiceDTO;
import com.example.billingapp.model.enums.InvoiceStatus;
import com.example.billingapp.repository.CustomerRepository;
import com.example.billingapp.repository.InvoiceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;

    public InvoiceService(InvoiceRepository invoiceRepository, CustomerRepository customerRepository) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
    }

    public Invoice createInvoice(Invoice invoice) {
        if (invoice.getCustomer() == null || !customerRepository.existsById(invoice.getCustomer().getId())) {
            throw new BusinessRuleViolationException("An invoice cannot be created without a valid existing customer");
        }
        if (invoice.getAmount() <= 0) {
            throw new BusinessRuleViolationException("Amount must be positive and non-zero");
        }
        if (invoice.getDueDate().isBefore(LocalDate.now().plusDays(1))) {
            throw new BusinessRuleViolationException("DueDate must be in the future");
        }
        return invoiceRepository.save(invoice);
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
    }

    public void deleteInvoice(Long id) {
        Invoice invoice = getInvoiceById(id);
        if (!invoice.getPayments().isEmpty()) {
            throw new BusinessRuleViolationException("An invoice with payments cannot be deleted");
        }
        invoiceRepository.delete(invoice);
    }

    public List<OverdueInvoiceDTO> getOverdueInvoices(Long customerId, LocalDate startDate, LocalDate endDate) {
        List<Invoice> invoices = invoiceRepository.findAll();
        List<OverdueInvoiceDTO> overdueList = new ArrayList<>();
        for (Invoice invoice : invoices) {
            InvoiceStatus status = invoice.getStatus();
            if (invoice.getDueDate().isBefore(LocalDate.now()) && status != InvoiceStatus.PAID) {
                if (customerId != null && !invoice.getCustomer().getId().equals(customerId)) continue;
                if (startDate != null && invoice.getCreatedAt().toLocalDate().isBefore(startDate)) continue;
                if (endDate != null && invoice.getCreatedAt().toLocalDate().isAfter(endDate)) continue;

                double paid = invoice.getPayments().stream().mapToDouble(p -> p.getAmount()).sum();
                OverdueInvoiceDTO dto = new OverdueInvoiceDTO();
                dto.setInvoiceNumber("INV" + invoice.getId());
                dto.setCustomerName(invoice.getCustomer().getName());
                dto.setAmount(invoice.getAmount());
                dto.setAmountPaid(paid);
                dto.setBalance(invoice.getAmount() - paid);
                dto.setDueDate(invoice.getDueDate().toString());
                dto.setDaysOverdue((int) ChronoUnit.DAYS.between(invoice.getDueDate(), LocalDate.now()));
                dto.setStatus(status.toString());
                overdueList.add(dto);
            }
        }
        return overdueList;
    }
}
