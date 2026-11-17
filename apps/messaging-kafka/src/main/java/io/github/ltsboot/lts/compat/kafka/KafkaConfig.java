package io.github.ltsboot.lts.compat.kafka;

import static io.github.ltsboot.lts.compat.kafka.MessagingKafkaApplication.ORDERS_TOPIC;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import org.apache.kafka.clients.admin.NewTopic;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic ordersTopic() {
        return TopicBuilder.name(ORDERS_TOPIC).partitions(1).replicas(1).build();
    }
}
