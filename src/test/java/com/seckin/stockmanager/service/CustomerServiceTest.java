package com.seckin.stockmanager.service;

import com.seckin.stockmanager.dto.CustomerDTO;
import com.seckin.stockmanager.exception.CustomerExistsException;
import com.seckin.stockmanager.exception.ResourceNotFoundException;
import com.seckin.stockmanager.model.Customer;
import com.seckin.stockmanager.model.CustomerRole;
import com.seckin.stockmanager.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getCustomerByUsername_ShouldReturnCustomer_WhenCustomerExists() {
        String username = "testUser";
        Customer customer = new Customer(username, "password");

        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(customer));

        Customer result = customerService.getCustomer(username);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(customerRepository, times(1)).findByUsername(username);
    }

    @Test
    void getCustomerByUsername_ShouldReturnNull_WhenCustomerDoesNotExist() {
        String username = "nonexistentUser";

        when(customerRepository.findByUsername(username)).thenReturn(Optional.empty());

        Customer result = customerService.getCustomer(username);

        assertNull(result);
        verify(customerRepository, times(1)).findByUsername(username);
    }

    @Test
    void getCustomerById_ShouldReturnCustomer_WhenCustomerExists() {
        Long customerId = 1L;
        Customer customer = new Customer("testUser", "password");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        Customer result = customerService.getCustomer(customerId);

        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
        verify(customerRepository, times(1)).findById(customerId);
    }

    @Test
    void getCustomerById_ShouldReturnNull_WhenCustomerDoesNotExist() {
        Long customerId = 1L;

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        Customer result = customerService.getCustomer(customerId);

        assertNull(result);
        verify(customerRepository, times(1)).findById(customerId);
    }

    @Test
    void addNewCustomer_ShouldAddCustomer_WhenCustomerDoesNotExist() {
        String username = "newUser";
        String password = "password";
        Customer customer = new Customer(username, "{noop}" + password);

        when(customerRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        CustomerDTO result = customerService.addNewCustomer(username, password);

        assertNotNull(result);
        assertEquals(username, result.username);
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void addNewCustomer_ShouldThrowException_WhenCustomerAlreadyExists() {
        String username = "existingUser";
        String password = "password";
        Customer existingCustomer = new Customer(username, "{noop}" + password);

        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(existingCustomer));

        assertThrows(CustomerExistsException.class, () -> customerService.addNewCustomer(username, password));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void getCustomerId_ShouldReturnCustomerId_WhenCustomerExists() {
        String username = "testUser";
        Customer customer = new Customer(username, "password");
        customer.setId(1L);

        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(customer));

        Long result = customerService.getCustomerId(username);

        assertEquals(1L, result);
        verify(customerRepository, times(1)).findByUsername(username);
    }

    @Test
    void getCustomerId_ShouldThrowException_WhenCustomerDoesNotExist() {
        String username = "nonexistentUser";

        when(customerRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.getCustomerId(username));
    }

    @Test
    void validateUserAuthenticated_ShouldAllowAccess_WhenUserIsAdmin() {
        String customerUserName = "testUser";
        String authenticatedUserName = "adminUser";
        Customer adminUser = new Customer(authenticatedUserName, "password");
        adminUser.setRole(CustomerRole.ADMIN);

        when(authentication.getName()).thenReturn(authenticatedUserName);
        when(customerRepository.findByUsername(authenticatedUserName)).thenReturn(Optional.of(adminUser));

        assertDoesNotThrow(() -> customerService.validateUserAuthenticated(customerUserName, authentication));
    }

    @Test
    void validateUserAuthenticated_ShouldAllowAccess_WhenUserAccessesOwnData() {
        String customerUserName = "testUser";
        Customer customer = new Customer(customerUserName, "password");

        when(authentication.getName()).thenReturn(customerUserName);
        when(customerRepository.findByUsername(customerUserName)).thenReturn(Optional.of(customer));

        assertDoesNotThrow(() -> customerService.validateUserAuthenticated(customerUserName, authentication));
    }

    @Test
    void validateUserAuthenticated_ShouldDenyAccess_WhenUserAccessesDifferentUser() {
        String customerUserName = "testUser";
        String authenticatedUserName = "otherUser";
        Customer otherUser = new Customer(authenticatedUserName, "password");
        otherUser.setRole(CustomerRole.CUSTOMER);

        when(authentication.getName()).thenReturn(authenticatedUserName);
        when(customerRepository.findByUsername(authenticatedUserName)).thenReturn(Optional.of(otherUser));

        assertThrows(AccessDeniedException.class, () -> customerService.validateUserAuthenticated(customerUserName, authentication));
    }
}
