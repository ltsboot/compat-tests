package io.github.ltsboot.lts.compat.kafka;

import static io.github.ltsboot.lts.compat.kafka.MessagingKafkaApplication.ORDERS_TOPIC;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderListener {

    private final Map<String, List<String>> eventsByOrder = new ConcurrentHashMap<>();

    @KafkaListener(topics = ORDERS_TOPIC, groupId = "messaging-kafka-tests")
    public void onOrderEvent(ConsumerRecord<String, String> record) {
        eventsByOrder.computeIfAbsent(record.key(), key -> new CopyOnWriteArrayList<>()).add(record.value());
    }

    public List<String> eventsFor(String orderId) {
        List<String> events = eventsByOrder.get(orderId);
        return events == null ? Collections.<String>emptyList() : events;
    }
}
