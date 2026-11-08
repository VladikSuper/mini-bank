package com.example.bank.service;

import com.example.bank.exception.AccountNotFoundException;
import com.example.bank.exception.InsufficientFundsException;
import com.example.bank.model.Account;
import com.example.bank.notification.NotificationSender;
import com.example.bank.repository.AccountRepository;
import com.example.bank.transaction.MyTransactional;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BankServiceImpl implements BankService {

    private final AccountRepository accountRepository;
    private final List<NotificationSender> notificationSenders;
    private int counter = 0;

    public BankServiceImpl(AccountRepository accountRepository,
                           List<NotificationSender> notificationSenders) {
        this.accountRepository = accountRepository;
        this.notificationSenders = notificationSenders;
        System.out.println("[create] BankServiceImpl");
    }

    @PostConstruct
    public void initCounter() {
        for (Account account : accountRepository.findAll()) {
            int number = Integer.parseInt(account.getId().substring(1));
            counter = Math.max(counter, number);
        }
    }

    @Override
    public Account openAccount(String owner, BigDecimal initialBalance) {
        if (initialBalance.signum() < 0) {
            throw new IllegalArgumentException("Начальный баланс не может быть меньше 0: " + initialBalance);
        }

        Account account = new Account("a" + (++counter), owner, initialBalance);
        accountRepository.save(account);
        return account;
    }

    @Override
    @MyTransactional
    public void deposit(String accountId, BigDecimal amount) {
        checkPositive(amount);

        Account account = findAccount(accountId);
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
    }

    @Override
    @MyTransactional
    public void withdraw(String accountId, BigDecimal amount) {
        checkPositive(amount);

        Account account = findAccount(accountId);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(accountId, account.getBalance(), amount);
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
    }

    @Override
    @MyTransactional
    public void transfer(String fromId, String toId, BigDecimal amount) {
        withdraw(fromId, amount);
        deposit(toId, amount);

        String message = "Перевод " + amount + " с " + fromId + " на " + toId;
        for (NotificationSender sender : notificationSenders) {
            sender.send(message);
        }
    }

    @Override
    public BigDecimal getBalance(String accountId) {
        return findAccount(accountId).getBalance();
    }

    private Account findAccount(String id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    private void checkPositive(BigDecimal amount) {
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Сумма должна быть больше нуля: " + amount);
        }
    }
}
