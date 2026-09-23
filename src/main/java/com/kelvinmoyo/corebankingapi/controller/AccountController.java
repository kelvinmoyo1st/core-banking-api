package com.kelvinmoyo.corebankingapi.controller;

import com.kelvinmoyo.corebankingapi.dto.AccountCreationRequest;
import com.kelvinmoyo.corebankingapi.dto.AccountResponse;
import com.kelvinmoyo.corebankingapi.dto.TransactionRequest;
import com.kelvinmoyo.corebankingapi.dto.TransactionResponse;
import com.kelvinmoyo.corebankingapi.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Accounts", description = "Account creation and transactional operations")
@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(summary = "Create an account", description = "Creates a new account for an existing customer, always starting at a zero balance.")
    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody AccountCreationRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "List all accounts (admin only)", description = "Returns every account in the system. Requires the ADMIN role.")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all-accounts")
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @Operation(summary = "Deposit funds", description = "Adds funds to an account atomically. Pass an Idempotency-Key header to safely retry without double-processing.")
    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @Valid @RequestBody TransactionRequest request,
            @Parameter(description = "Client-generated key; a repeated key returns the original cached response instead of reprocessing")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        TransactionResponse response = accountService.deposit(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Withdraw funds", description = "Deducts funds from an account atomically, rejecting the request with 409 if funds are insufficient. Pass an Idempotency-Key header to safely retry without double-processing.")
    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @Valid @RequestBody TransactionRequest request,
            @Parameter(description = "Client-generated key; a repeated key returns the original cached response instead of reprocessing")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        TransactionResponse response = accountService.withdraw(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
