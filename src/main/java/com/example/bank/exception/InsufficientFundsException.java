package com.example.bank.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String accountId, BigDecimal balance, BigDecimal required) {
        super("Недостаточно средств на аккаунте " + accountId
                + " на счете: " + balance + ", нужно: " + required);
    }
}
