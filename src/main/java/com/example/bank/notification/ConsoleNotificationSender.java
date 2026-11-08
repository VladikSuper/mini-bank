package com.example.bank.notification;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class ConsoleNotificationSender implements NotificationSender {

    @Override
    public void send(String message) {
        System.out.println("[консоль] " + message);
    }
}
