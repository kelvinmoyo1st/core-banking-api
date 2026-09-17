package com.kelvinmoyo.corebankingapi.exception;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(Long customerId) {
        super("Customer not found: " + customerId);
    }

    public CustomerNotFoundException(String email) {
        super("Customer not found: " + email);
    }
}
