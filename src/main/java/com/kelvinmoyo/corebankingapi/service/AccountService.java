package com.kelvinmoyo.corebankingapi.service;

import com.kelvinmoyo.corebankingapi.dto.AccountCreationRequest;
import com.kelvinmoyo.corebankingapi.dto.AccountMapper;
import com.kelvinmoyo.corebankingapi.dto.AccountResponse;
import com.kelvinmoyo.corebankingapi.dto.TransactionMapper;
import com.kelvinmoyo.corebankingapi.dto.TransactionRequest;
import com.kelvinmoyo.corebankingapi.dto.TransactionResponse;
import com.kelvinmoyo.corebankingapi.entity.Account;
import com.kelvinmoyo.corebankingapi.entity.Customer;
import com.kelvinmoyo.corebankingapi.entity.Transaction;
import com.kelvinmoyo.corebankingapi.entity.TransactionType;
import com.kelvinmoyo.corebankingapi.exception.AccountNotFoundException;
import com.kelvinmoyo.corebankingapi.exception.CustomerNotFoundException;
import com.kelvinmoyo.corebankingapi.exception.InsufficientFundsException;
import com.kelvinmoyo.corebankingapi.repository.AccountRepository;
import com.kelvinmoyo.corebankingapi.repository.CustomerRepository;
import com.kelvinmoyo.corebankingapi.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final AccountMapper accountMapper;
    private final TransactionMapper transactionMapper;

    public AccountService(AccountRepository accountRepository,
                           CustomerRepository customerRepository,
                           TransactionRepository transactionRepository,
                           AccountMapper accountMapper,
                           TransactionMapper transactionMapper) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.accountMapper = accountMapper;
        this.transactionMapper = transactionMapper;
    }

    public AccountResponse createAccount(AccountCreationRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(request.getCustomerId()));

        String accountNumber = generateAccountNumber();

        Account account = new Account(accountNumber, customer, request.getAccountType(), BigDecimal.ZERO);
        Account saved = accountRepository.save(account);

        return accountMapper.toResponse(saved);
    }

    @Transactional
    public TransactionResponse deposit(TransactionRequest request) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException(request.getAccountId()));

        BigDecimal newBalance = account.getBalance().add(request.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        Transaction transaction = new Transaction(account, TransactionType.DEPOSIT, request.getAmount(), newBalance);
        Transaction saved = transactionRepository.save(transaction);

        return transactionMapper.toResponse(saved);
    }

    @Transactional
    public TransactionResponse withdraw(TransactionRequest request) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException(request.getAccountId()));

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException(request.getAccountId(), request.getAmount(), account.getBalance());
        }

        BigDecimal newBalance = account.getBalance().subtract(request.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        Transaction transaction = new Transaction(account, TransactionType.WITHDRAWAL, request.getAmount(), newBalance);
        Transaction saved = transactionRepository.save(transaction);

        return transactionMapper.toResponse(saved);
    }

    private String generateAccountNumber() {
        return "ACC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
