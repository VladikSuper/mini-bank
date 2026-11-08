package com.example.bank.repository;

import com.example.bank.model.Account;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@Profile("prod")
public class FileAccountRepository implements AccountRepository {

    private final Path file = Path.of("accounts.csv");
    private final Map<String, Account> accounts = new LinkedHashMap<>();

    @PostConstruct
    public void load() {
        if (!Files.exists(file)) {
            System.out.println("[file] Файла " + file.toAbsolutePath() + " ещё нет, начинаем с пустого банка");
            return;
        }

        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(";");
                Account account = new Account(parts[0], parts[1], new BigDecimal(parts[2]));
                accounts.put(account.getId(), account);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать " + file, e);
        }

        System.out.println("[file] Загружено счетов из файла: " + accounts.size());
    }

    @Override
    public void save(Account account) {
        accounts.put(account.getId(), account);
        writeToFile();
    }

    @Override
    public Optional<Account> findById(String id) {
        return Optional.ofNullable(accounts.get(id));
    }

    @Override
    public List<Account> findAll() {
        return new ArrayList<>(accounts.values());
    }

    private void writeToFile() {
        List<String> lines = new ArrayList<>();
        for (Account account : accounts.values()) {
            lines.add(account.getId() + ";" + account.getOwner() + ";" + account.getBalance());
        }

        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось сохранить " + file, e);
        }
    }
}
