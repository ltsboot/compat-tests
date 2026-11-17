package io.github.ltsboot.lts.compat.kafka;

import static io.github.ltsboot.lts.compat.kafka.MessagingKafkaApplication.ORDERS_TOPIC;

import java.util.concurrent.TimeUnit;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public OrderPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /** Sends synchronously: returns once the broker has acknowledged the record. */
    public void publish(String orderId, String event) {
        try {
            kafkaTemplate.send(ORDERS_TOPIC, orderId, event).get(10, TimeUnit.SECONDS);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not publish event for order " + orderId, ex);
        }
    }
}
