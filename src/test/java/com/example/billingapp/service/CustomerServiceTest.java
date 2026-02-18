package com.example.billingapp.service;

import com.example.billingapp.exception.BusinessRuleViolationException;
import com.example.billingapp.exception.ResourceNotFoundException;
import com.example.billingapp.model.Customer;
import com.example.billingapp.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");
        customer.setEmail("john@example.com");
        customer.setPhone("123-456-7890");
    }

    @Test
    void createCustomer_Success() {
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        Customer result = customerService.createCustomer(customer);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        verify(customerRepository).existsByEmail("john@example.com");
        verify(customerRepository).save(customer);
    }

    @Test
    void createCustomer_MissingName_ThrowsException() {
        customer.setName(null);

        assertThrows(BusinessRuleViolationException.class, () -> {
            customerService.createCustomer(customer);
        });

        verify(customerRepository, never()).save(any());
    }

    @Test
    void createCustomer_MissingEmail_ThrowsException() {
        customer.setEmail(null);

        assertThrows(BusinessRuleViolationException.class, () -> {
            customerService.createCustomer(customer);
        });

        verify(customerRepository, never()).save(any());
    }

    @Test
    void createCustomer_DuplicateEmail_ThrowsException() {
        when(customerRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(BusinessRuleViolationException.class, () -> {
            customerService.createCustomer(customer);
        });

        verify(customerRepository, never()).save(any());
    }

    @Test
    void getAllCustomers_Success() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findAll()).thenReturn(customers);

        List<Customer> result = customerService.getAllCustomers();

        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getName());
        verify(customerRepository).findAll();
    }

    @Test
    void getCustomerById_Success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        Customer result = customerService.getCustomerById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(customerRepository).findById(1L);
    }

    @Test
    void getCustomerById_NotFound_ThrowsException() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            customerService.getCustomerById(999L);
        });
    }

    @Test
    void updateCustomer_Success() {
        Customer updatedCustomer = new Customer();
        updatedCustomer.setName("Jane Doe");
        updatedCustomer.setEmail("jane@example.com");
        updatedCustomer.setPhone("987-654-3210");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        Customer result = customerService.updateCustomer(1L, updatedCustomer);

        assertNotNull(result);
        verify(customerRepository).findById(1L);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void updateCustomer_DuplicateEmail_ThrowsException() {
        Customer updatedCustomer = new Customer();
        updatedCustomer.setEmail("existing@example.com");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(BusinessRuleViolationException.class, () -> {
            customerService.updateCustomer(1L, updatedCustomer);
        });
    }

    @Test
    void deleteCustomer_Success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        doNothing().when(customerRepository).delete(customer);

        customerService.deleteCustomer(1L);

        verify(customerRepository).findById(1L);
        verify(customerRepository).delete(customer);
    }
}
