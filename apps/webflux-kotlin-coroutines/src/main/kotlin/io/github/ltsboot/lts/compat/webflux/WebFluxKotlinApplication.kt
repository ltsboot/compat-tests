package io.github.ltsboot.lts.compat.webflux

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WebFluxKotlinApplication

fun main(args: Array<String>) {
    runApplication<WebFluxKotlinApplication>(*args)
}
