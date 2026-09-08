package com.kelvinmoyo.corebankingapi.dto;

import com.kelvinmoyo.corebankingapi.entity.AccountType;
import jakarta.validation.constraints.NotNull;

public class AccountCreationRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    public AccountCreationRequest() {
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }
}
