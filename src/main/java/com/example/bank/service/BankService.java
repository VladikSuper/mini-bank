package com.example.bank.service;

import com.example.bank.model.Account;

import java.math.BigDecimal;

public interface BankService {

    Account openAccount(String owner, BigDecimal initialBalance);

    void deposit(String accountId, BigDecimal amount);

    void withdraw(String accountId, BigDecimal amount);

    void transfer(String fromId, String toId, BigDecimal amount);

    BigDecimal getBalance(String accountId);
}
