package com.kelvinmoyo.corebankingapi.controller;

import com.kelvinmoyo.corebankingapi.dto.CustomerRegistrationRequest;
import com.kelvinmoyo.corebankingapi.dto.CustomerResponse;
import com.kelvinmoyo.corebankingapi.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Customers", description = "Customer registration and profile management")
@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Operation(summary = "Register a new customer",
            description = "Creates a customer with the CUSTOMER role by default. The password is hashed before storage and never returned in the response.")
    @PostMapping
    public ResponseEntity<CustomerResponse> register(@Valid @RequestBody CustomerRegistrationRequest request) {
        CustomerResponse response = customerService.registerCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get the current customer's profile",
            description = "Returns the profile of whichever customer the bearer token identifies. Accessible to any authenticated role.")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> getCurrentCustomer(Authentication authentication) {
        return ResponseEntity.ok(customerService.getProfile(authentication.getName()));
    }
}
