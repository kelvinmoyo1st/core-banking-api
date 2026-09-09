package com.kelvinmoyo.corebankingapi.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(Long accountId, BigDecimal requested, BigDecimal available) {
        super("Insufficient funds in account " + accountId + ": requested " + requested + ", available " + available);
    }
}
