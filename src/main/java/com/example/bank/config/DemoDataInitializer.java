package com.example.bank.config;

import com.example.bank.model.Account;
import com.example.bank.service.BankService;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Profile("dev")
public class DemoDataInitializer {

    private final BankService bank;

    public DemoDataInitializer(BankService bank) {
        this.bank = bank;
        System.out.println("[create] DemoDataInitializer");
    }

    @PostConstruct
    public void createTestAccounts() {
        Account first = bank.openAccount("Влад (Test)", new BigDecimal("1000"));
        Account second = bank.openAccount("Иван (Test)", new BigDecimal("5000"));
        Account third = bank.openAccount("Коля (Test)", BigDecimal.ZERO);

        System.out.println("[init] Тестовые счета созданы:");
        System.out.println("    " + first);
        System.out.println("    " + second);
        System.out.println("    " + third);
    }
}
