package com.example.bank;

import com.example.bank.config.AppConfig;
import com.example.bank.model.Account;
import com.example.bank.repository.AccountRepository;
import com.example.bank.service.BankService;
import com.example.bank.service.BankServiceImpl;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.Arrays;


public class Main {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        try (var ctx = createContext()) {

            BankService bank = ctx.getBean(BankService.class);
            AccountRepository repository = ctx.getBean(AccountRepository.class);

            section("2. Открываем счета");
            Account vlad = bank.openAccount("Влад", new BigDecimal("1000"));
            Account anya = bank.openAccount("Аня", new BigDecimal("3000"));
            System.out.println("  " + vlad);
            System.out.println("  " + anya);
            String vladId = vlad.getId();
            String anyaId = anya.getId();

            section("3. Пополнение и снятие");
            bank.deposit(vladId, new BigDecimal("500"));
            check("Влад после пополнения на 500", bank.getBalance(vladId), "1500");
            bank.withdraw(anyaId, new BigDecimal("1000"));
            check("Аня после снятия 1000", bank.getBalance(anyaId), "2000");

            section("4. Перевод (смотри порядок уведомлений: сначала email, потом консоль)");
            bank.transfer(vladId, anyaId, new BigDecimal("300"));
            check("Влад после перевода", bank.getBalance(vladId), "1200");
            check("Аня после перевода", bank.getBalance(anyaId), "2300");

            section("5. Ошибки: неправильные операции отклоняются");
            expectError("Снять больше, чем есть",
                    () -> bank.withdraw(vladId, new BigDecimal("99999")));
            expectError("Пополнить на отрицательную сумму",
                    () -> bank.deposit(vladId, new BigDecimal("-50")));
            expectError("Пополнить на ноль",
                    () -> bank.deposit(vladId, BigDecimal.ZERO));
            expectError("Открыть счёт с отрицательным балансом",
                    () -> bank.openAccount("Хакер", new BigDecimal("-1")));
            expectError("Баланс несуществующего счёта",
                    () -> bank.getBalance("a999"));
            check("Влад не пострадал", bank.getBalance(vladId), "1200");

            section("6. @MyTransactional: перевод на несуществующий счёт откатывается");
            System.out.println("  Без отката деньги бы списались с Влада и пропали.");
            expectError("Перевод 100 от Влада на a999",
                    () -> bank.transfer(vladId, "a999", new BigDecimal("100")));
            check("Влад: деньги на месте", bank.getBalance(vladId), "1200");

            section("7. Прокси под микроскопом");
            System.out.println("  Это JDK-прокси?      " + Proxy.isProxyClass(bank.getClass()));
            System.out.println("  Интерфейсы прокси:   " + Arrays.toString(bank.getClass().getInterfaces()));
            expectNoBean("getBean(BankServiceImpl.class)", ctx, BankServiceImpl.class);

            section("8. Какие бины создал Spring");
            for (String name : ctx.getBeanDefinitionNames()) {
                if (!name.startsWith("org.springframework")) {
                    System.out.println("    " + name);
                }
            }

            section("Итог");
            System.out.println("  Проверок пройдено: " + passed + ", провалено: " + failed);
            System.out.println();
            System.out.println("Закрываем контекст (смотри [destroy] ниже, если профиль dev):");
        }
    }

    private static AnnotationConfigApplicationContext createContext() {
        var ctx = new AnnotationConfigApplicationContext();

        ctx.register(AppConfig.class);
        ctx.refresh();
        return ctx;
    }

    private static void section(String title) {
        System.out.println();
        System.out.println(title);
    }

    private static void check(String title, BigDecimal actual, String expected) {
        if (actual.compareTo(new BigDecimal(expected)) == 0) {
            passed++;
            System.out.println(title + ": " + actual);
        } else {
            failed++;
            System.out.println(title + ": " + actual + "   (ожидалось " + expected + ")");
        }
    }

    private static void expectError(String title, Runnable action) {
        try {
            action.run();
            failed++;
            System.out.println(title + ": ошибки не было, а должна была быть");
        } catch (RuntimeException e) {
            passed++;
            System.out.println(title + " → " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private static void expectNoBean(String title, AnnotationConfigApplicationContext ctx, Class<?> type) {
        try {
            ctx.getBean(type);
            failed++;
            System.out.println(title + ": бин нашёлся, а не должен был");
        } catch (NoSuchBeanDefinitionException e) {
            passed++;
            System.out.println(title + " → NoSuchBeanDefinitionException (прокси не является " + type.getSimpleName() + ")");
        }
    }
}
