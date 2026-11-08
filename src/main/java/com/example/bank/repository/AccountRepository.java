package com.example.bank.repository;

import com.example.bank.model.Account;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {

    void save(Account account);

    Optional<Account> findById(String id);

    List<Account> findAll();
}
