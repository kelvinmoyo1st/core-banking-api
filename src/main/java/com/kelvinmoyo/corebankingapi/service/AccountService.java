package com.kelvinmoyo.corebankingapi.service;

import com.kelvinmoyo.corebankingapi.dto.AccountCreationRequest;
import com.kelvinmoyo.corebankingapi.dto.AccountMapper;
import com.kelvinmoyo.corebankingapi.dto.AccountResponse;
import com.kelvinmoyo.corebankingapi.entity.Account;
import com.kelvinmoyo.corebankingapi.entity.Customer;
import com.kelvinmoyo.corebankingapi.exception.CustomerNotFoundException;
import com.kelvinmoyo.corebankingapi.repository.AccountRepository;
import com.kelvinmoyo.corebankingapi.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final AccountMapper accountMapper;

    public AccountService(AccountRepository accountRepository, CustomerRepository customerRepository, AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.accountMapper = accountMapper;
    }

    public AccountResponse createAccount(AccountCreationRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(request.getCustomerId()));

        String accountNumber = generateAccountNumber();

        Account account = new Account(accountNumber, customer, request.getAccountType(), BigDecimal.ZERO);
        Account saved = accountRepository.save(account);

        return accountMapper.toResponse(saved);
    }

    private String generateAccountNumber() {
        return "ACC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
