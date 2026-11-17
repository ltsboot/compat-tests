package io.github.ltsboot.lts.compat.webflux

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(GreetingController::class)
@Import(GreetingService::class)
class GreetingControllerTests {

    @Autowired
    lateinit var client: WebTestClient

    @Test
    fun suspendHandlerReturnsGreeting() {
        client.get().uri("/api/greetings/Ada")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.message").isEqualTo("Hello, Ada!")
            .jsonPath("$.sequence").isEqualTo(0)
    }

    @Test
    fun flowEndpointEmitsEveryElementInOrder() {
        client.get().uri("/api/greetings/stream?count=3")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Greeting::class.java)
            .hasSize(3)
            .returnResult()
    }
}
