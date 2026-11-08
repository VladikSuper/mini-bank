package com.example.bank.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(String accountId) {
        super("Аккаунт не найден: " + accountId);
    }
}
