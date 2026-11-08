package com.example.bank.repository;

import com.example.bank.model.Account;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@Profile("dev")
public class InMemoryAccountRepository implements AccountRepository {

    private final Map<String, Account> accounts = new LinkedHashMap<>();

    public InMemoryAccountRepository() {
        System.out.println("[create] InMemoryAccountRepository");
    }

    @Override
    public void save(Account account) {
        accounts.put(account.getId(), account);
    }

    @Override
    public Optional<Account> findById(String id) {
        return Optional.ofNullable(accounts.get(id));
    }

    @Override
    public List<Account> findAll() {
        return new ArrayList<>(accounts.values());
    }

    @PreDestroy
    public void printFinalBalances() {
        System.out.println("[destroy] Итоговые балансы:");
        for (Account account : accounts.values()) {
            System.out.println("    " + account);
        }
    }
}
