package io.github.ltsboot.lts.compat.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MessagingKafkaApplication {

    public static final String ORDERS_TOPIC = "orders";

    public static void main(String[] args) {
        SpringApplication.run(MessagingKafkaApplication.class, args);
    }
}
