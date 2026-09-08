package com.kelvinmoyo.corebankingapi.exception;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(Long customerId) {
        super("Customer not found: " + customerId);
    }
}
