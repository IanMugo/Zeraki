package com.example.billingapp.repository;

import com.example.billingapp.model.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CustomerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void saveCustomer_Success() {
        Customer customer = new Customer();
        customer.setName("John Doe");
        customer.setEmail("john@example.com");
        customer.setPhone("123-456-7890");

        Customer saved = customerRepository.save(customer);

        assertNotNull(saved.getId());
        assertEquals("John Doe", saved.getName());
        assertEquals("john@example.com", saved.getEmail());
    }

    @Test
    void findById_Success() {
        Customer customer = new Customer();
        customer.setName("John Doe");
        customer.setEmail("john@example.com");
        Customer saved = entityManager.persistAndFlush(customer);

        Optional<Customer> found = customerRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
    }

    @Test
    void existsByEmail_True() {
        Customer customer = new Customer();
        customer.setName("John Doe");
        customer.setEmail("john@example.com");
        entityManager.persistAndFlush(customer);

        boolean exists = customerRepository.existsByEmail("john@example.com");

        assertTrue(exists);
    }

    @Test
    void existsByEmail_False() {
        boolean exists = customerRepository.existsByEmail("nonexistent@example.com");

        assertFalse(exists);
    }

    @Test
    void findAll_Success() {
        Customer customer1 = new Customer();
        customer1.setName("John Doe");
        customer1.setEmail("john@example.com");

        Customer customer2 = new Customer();
        customer2.setName("Jane Smith");
        customer2.setEmail("jane@example.com");

        entityManager.persistAndFlush(customer1);
        entityManager.persistAndFlush(customer2);

        assertEquals(2, customerRepository.findAll().size());
    }

    @Test
    void deleteCustomer_Success() {
        Customer customer = new Customer();
        customer.setName("John Doe");
        customer.setEmail("john@example.com");
        Customer saved = entityManager.persistAndFlush(customer);

        customerRepository.delete(saved);
        entityManager.flush();

        Optional<Customer> found = customerRepository.findById(saved.getId());
        assertFalse(found.isPresent());
    }
}
