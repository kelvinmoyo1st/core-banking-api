package com.kelvinmoyo.corebankingapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kelvinmoyo.corebankingapi.dto.AccountCreationRequest;
import com.kelvinmoyo.corebankingapi.dto.AccountMapper;
import com.kelvinmoyo.corebankingapi.dto.AccountResponse;
import com.kelvinmoyo.corebankingapi.dto.TransactionMapper;
import com.kelvinmoyo.corebankingapi.dto.TransactionRequest;
import com.kelvinmoyo.corebankingapi.dto.TransactionResponse;
import com.kelvinmoyo.corebankingapi.entity.Account;
import com.kelvinmoyo.corebankingapi.entity.Customer;
import com.kelvinmoyo.corebankingapi.entity.IdempotencyKey;
import com.kelvinmoyo.corebankingapi.entity.Transaction;
import com.kelvinmoyo.corebankingapi.entity.TransactionType;
import com.kelvinmoyo.corebankingapi.exception.AccountNotFoundException;
import com.kelvinmoyo.corebankingapi.exception.CustomerNotFoundException;
import com.kelvinmoyo.corebankingapi.exception.InsufficientFundsException;
import com.kelvinmoyo.corebankingapi.repository.AccountRepository;
import com.kelvinmoyo.corebankingapi.repository.CustomerRepository;
import com.kelvinmoyo.corebankingapi.repository.IdempotencyKeyRepository;
import com.kelvinmoyo.corebankingapi.repository.TransactionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final AccountMapper accountMapper;
    private final TransactionMapper transactionMapper;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public AccountService(AccountRepository accountRepository,
                          CustomerRepository customerRepository,
                          TransactionRepository transactionRepository,
                          AccountMapper accountMapper,
                          TransactionMapper transactionMapper,
                          IdempotencyKeyRepository idempotencyKeyRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.accountMapper = accountMapper;
        this.transactionMapper = transactionMapper;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
    }

    public AccountResponse createAccount(AccountCreationRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(request.getCustomerId()));

        String accountNumber = generateAccountNumber();

        Account account = new Account(accountNumber, customer, request.getAccountType(), BigDecimal.ZERO);
        Account saved = accountRepository.save(account);

        return accountMapper.toResponse(saved);
    }

    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(accountMapper::toResponse)
                .toList();
    }

    @Transactional
    public TransactionResponse deposit(TransactionRequest request, String idempotencyKey) {
        TransactionResponse cached = checkIdempotency(idempotencyKey);
        if (cached != null) return cached;

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException(request.getAccountId()));

        BigDecimal newBalance = account.getBalance().add(request.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        Transaction transaction = new Transaction(account, TransactionType.DEPOSIT, request.getAmount(), newBalance);
        Transaction saved = transactionRepository.save(transaction);

        TransactionResponse response = transactionMapper.toResponse(saved);
        return storeIdempotency(idempotencyKey, response);
    }

    @Transactional
    public TransactionResponse withdraw(TransactionRequest request, String idempotencyKey) {
        TransactionResponse cached = checkIdempotency(idempotencyKey);
        if (cached != null) return cached;

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

        TransactionResponse response = transactionMapper.toResponse(saved);
        return storeIdempotency(idempotencyKey, response);
    }

    private TransactionResponse checkIdempotency(String idempotencyKey) {
        if (idempotencyKey == null) return null;
        return idempotencyKeyRepository.findByIdempotencyKey(idempotencyKey)
                .map(k -> deserialize(k.getResponseBody()))
                .orElse(null);
    }

    private TransactionResponse storeIdempotency(String idempotencyKey, TransactionResponse response) {
        if (idempotencyKey == null) return response;
        try {
            idempotencyKeyRepository.save(new IdempotencyKey(idempotencyKey, serialize(response)));
            return response;
        } catch (DataIntegrityViolationException e) {
            return idempotencyKeyRepository.findByIdempotencyKey(idempotencyKey)
                    .map(k -> deserialize(k.getResponseBody()))
                    .orElse(response);
        }
    }

    private String serialize(TransactionResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize idempotent response", e);
        }
    }

    private TransactionResponse deserialize(String json) {
        try {
            return objectMapper.readValue(json, TransactionResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize idempotent response", e);
        }
    }

    private String generateAccountNumber() {
        return "ACC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
