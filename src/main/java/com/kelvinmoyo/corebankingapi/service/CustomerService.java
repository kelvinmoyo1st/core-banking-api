package com.kelvinmoyo.corebankingapi.service;

import com.kelvinmoyo.corebankingapi.dto.CustomerMapper;
import com.kelvinmoyo.corebankingapi.dto.CustomerRegistrationRequest;
import com.kelvinmoyo.corebankingapi.dto.CustomerResponse;
import com.kelvinmoyo.corebankingapi.entity.Customer;
import com.kelvinmoyo.corebankingapi.exception.EmailAlreadyExistsException;
import com.kelvinmoyo.corebankingapi.repository.CustomerRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerService(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    public CustomerResponse registerCustomer(CustomerRegistrationRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        Customer customer = new Customer(request.getFirstName(), request.getLastName(), request.getEmail());

        try {
            Customer saved = customerRepository.save(customer);
            return customerMapper.toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
    }
}
