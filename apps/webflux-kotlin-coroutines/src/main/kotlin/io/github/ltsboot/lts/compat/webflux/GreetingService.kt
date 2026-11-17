package io.github.ltsboot.lts.compat.webflux

import kotlinx.coroutines.delay
import org.springframework.stereotype.Service

@Service
class GreetingService {

    suspend fun greet(name: String): Greeting {
        delay(5) // stands in for non-blocking I/O
        return Greeting("Hello, $name!", 0)
    }
}
