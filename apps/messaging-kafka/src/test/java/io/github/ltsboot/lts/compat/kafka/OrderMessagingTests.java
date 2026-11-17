package io.github.ltsboot.lts.compat.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class OrderMessagingTests {

    @Container
    static final ConfluentKafkaContainer KAFKA =
            new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.4"));

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }

    @Autowired
    private OrderPublisher publisher;

    @Autowired
    private OrderListener listener;

    @Test
    void publishedEventIsConsumedWithItsKey() {
        publisher.publish("order-1", "created");

        await().atMost(Duration.ofSeconds(60))
                .until(() -> listener.eventsFor("order-1").size() == 1);

        assertThat(listener.eventsFor("order-1")).containsExactly("created");
    }

    @Test
    void eventsWithTheSameKeyArriveInPublishOrder() {
        publisher.publish("order-2", "created");
        publisher.publish("order-2", "paid");
        publisher.publish("order-2", "shipped");

        await().atMost(Duration.ofSeconds(60))
                .until(() -> listener.eventsFor("order-2").size() == 3);

        assertThat(listener.eventsFor("order-2")).containsExactly("created", "paid", "shipped");
    }
}
