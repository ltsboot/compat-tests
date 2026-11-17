package io.github.ltsboot.lts.compat.webflux

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class GreetingApiIntegrationTests {

    @Autowired
    lateinit var client: WebTestClient

    @Test
    fun defaultFlowCountIsThree() {
        val greetings = client.get().uri("/api/greetings/stream")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Greeting::class.java)
            .returnResult()
            .responseBody

        assertThat(greetings).hasSize(3)
        assertThat(greetings!!.map { it.sequence }).containsExactly(1, 2, 3)
    }

    @Test
    fun pathVariableWithSpaceIsEncodedAndDecoded() {
        client.get().uri("/api/greetings/{name}", "Ada Lovelace")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.message").isEqualTo("Hello, Ada Lovelace!")
    }
}
