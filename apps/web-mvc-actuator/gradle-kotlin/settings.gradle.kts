// Settings for the Kotlin DSL build. The Spring Boot plugin version comes from gradle.properties.
pluginManagement {
    val bootVersion = providers.gradleProperty("bootVersion").get()
    plugins {
        id("org.springframework.boot") version bootVersion
    }
}

rootProject.name = "web-mvc-actuator"
