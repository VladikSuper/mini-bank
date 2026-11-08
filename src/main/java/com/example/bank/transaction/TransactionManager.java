package com.example.bank.transaction;

import com.example.bank.exception.AccountNotFoundException;
import com.example.bank.model.Account;
import com.example.bank.repository.AccountRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class TransactionManager {

    private final AccountRepository repository;

    public TransactionManager(AccountRepository repository) {
        this.repository = repository;
    }

    public Map<String, BigDecimal> begin() {
        Map<String, BigDecimal> snapshot = new HashMap<>();

        for (Account account : repository.findAll()) {
            snapshot.put(account.getId(), account.getBalance());
        }

        return snapshot;
    }

    public void rollback(Map<String, BigDecimal> snapshot) {
        for (Map.Entry<String, BigDecimal> entry : snapshot.entrySet()) {
            String id = entry.getKey();
            BigDecimal oldBalance = entry.getValue();

            Account account = repository.findById(id)
                    .orElseThrow(() -> new AccountNotFoundException(id));
            account.setBalance(oldBalance);
            repository.save(account);
        }

        System.out.println("ROLLBACK: балансы восстановлены");
    }
}
