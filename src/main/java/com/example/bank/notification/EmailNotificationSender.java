package com.example.bank.notification;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class EmailNotificationSender implements NotificationSender {

    @Override
    public void send(String message) {
        System.out.println("[email] " + message);
    }
}
