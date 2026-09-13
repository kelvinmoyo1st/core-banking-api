package com.kelvinmoyo.corebankingapi.service;

import com.kelvinmoyo.corebankingapi.dto.TransactionMapper;
import com.kelvinmoyo.corebankingapi.dto.TransactionResponse;
import com.kelvinmoyo.corebankingapi.entity.Transaction;
import com.kelvinmoyo.corebankingapi.exception.AccountNotFoundException;
import com.kelvinmoyo.corebankingapi.repository.AccountRepository;
import com.kelvinmoyo.corebankingapi.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;

    public TransactionService(TransactionRepository transactionRepository,
                               AccountRepository accountRepository,
                               TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.transactionMapper = transactionMapper;
    }

    public List<TransactionResponse> getTransactionHistory(Long accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException(accountId);
        }

        List<Transaction> transactions = transactionRepository.findByAccountId(accountId);

        return transactions.stream()
                .map(transactionMapper::toResponse)
                .toList();
    }
}
