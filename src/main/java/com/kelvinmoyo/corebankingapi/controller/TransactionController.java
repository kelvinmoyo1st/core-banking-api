package com.kelvinmoyo.corebankingapi.controller;

import com.kelvinmoyo.corebankingapi.dto.TransactionResponse;
import com.kelvinmoyo.corebankingapi.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Transactions", description = "Transaction history")
@RestController
@RequestMapping("/accounts")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(summary = "Get transaction history", description = "Returns every deposit and withdrawal for the given account, newest logic aside \u2014 an empty list means the account exists but has no transactions yet; a 404 means the account itself doesn't exist.")
    @GetMapping("/{accountId}/transactions")
    public ResponseEntity<List<TransactionResponse>> getHistory(@PathVariable Long accountId) {
        List<TransactionResponse> history = transactionService.getTransactionHistory(accountId);
        return ResponseEntity.ok(history);
    }
}
