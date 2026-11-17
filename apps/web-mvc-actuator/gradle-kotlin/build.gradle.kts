plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
    java
}

// Sources are shared with the Maven build and live one level up in ../src.
val bootVersion: String by project

group = "io.github.ltsboot.lts.compat"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:$bootVersion")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

sourceSets {
    main {
        java.setSrcDirs(listOf("../src/main/java"))
        resources.setSrcDirs(listOf("../src/main/resources"))
    }
    test {
        java.setSrcDirs(listOf("../src/test/java"))
        resources.setSrcDirs(listOf("../src/test/resources"))
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
