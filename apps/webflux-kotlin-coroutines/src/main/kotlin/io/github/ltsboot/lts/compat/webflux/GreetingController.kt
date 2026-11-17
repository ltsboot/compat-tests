package io.github.ltsboot.lts.compat.webflux

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/greetings")
class GreetingController(private val greetings: GreetingService) {

    @GetMapping("/{name}")
    suspend fun greet(@PathVariable name: String): Greeting = greetings.greet(name)

    @GetMapping("/stream")
    fun stream(@RequestParam(defaultValue = "3") count: Int): Flow<Greeting> = flow {
        for (i in 1..count) {
            delay(10)
            emit(Greeting("hello $i", i))
        }
    }
}
