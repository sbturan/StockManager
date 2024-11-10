package com.seckin.stockmanager.service;

import com.seckin.stockmanager.dto.CustomerDTO;
import com.seckin.stockmanager.exception.CustomerExistsException;
import com.seckin.stockmanager.exception.ResourceNotFoundException;
import com.seckin.stockmanager.model.Customer;
import com.seckin.stockmanager.model.CustomerRole;
import com.seckin.stockmanager.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);
    private CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer getCustomer(String username) {
        return customerRepository.findByUsername(username).orElse(null);
    }
    public Customer getCustomer(Long id) {
        return customerRepository.findById(id).orElse(null);
    }

    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

    public CustomerDTO addNewCustomer(String username, String password) {
        if (getCustomer(username) != null) {
            logger.error("Duplicate username:"+username);
            throw new CustomerExistsException("Customer Already Exists with given " +
                    "username");
        }

        Customer savedCustomer = save(new Customer(username, "{noop}"+password));
        return new CustomerDTO(savedCustomer.getId(), savedCustomer.getUsername());
    }

    public Long getCustomerId(String userName) {
        Customer customer = getCustomer(userName);
        if(customer==null){
            logger.error("Customer Not found with userName:" +userName);
            throw  new ResourceNotFoundException("Customer Not Found");
        }
        return customer.getId();
    }

    public void validateUserAuthenticated(String customerUserName,
                                          Authentication authentication) {
        String authenticatedUserName = authentication.getName();
        Customer authenticatedUser = getCustomer(authenticatedUserName);
        if (authenticatedUser == null ||
                (authenticatedUser.getRole() != CustomerRole.ADMIN
                        && !customerUserName.equals(authenticatedUserName))) {
            logger.error("Access request to different user");
            throw new AccessDeniedException("You can only access your own orders.");
        }
    }
}
