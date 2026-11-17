# Compatibility sample apps (T-7.1)

Six small Spring Boot 2.7 applications used to check that the ltsboot fork (`2.7.18.1-SNAPSHOT`)
behaves like the original `2.7.18` across the usual dependency and build tools. The same source tree is
built by Maven and by two Gradle builds (Groovy DSL and Kotlin DSL), so any difference between the two
Boot versions shows up in the same code.

| App | Covers |
|-----|--------|
| `web-mvc-actuator` | REST controller, request body validation, Actuator `health`/`info`; MockMvc and TestRestTemplate |
| `webflux-kotlin-coroutines` | Kotlin, `suspend` controllers, `Flow` endpoint, Kotlin Spring plugin; WebTestClient |
| `security` | Form login (`/web/**`) and JWT resource server (`/api/**`); spring-security-test |
| `data-jpa-flyway` | JPA entity and repository, Flyway migration, `ddl-auto=validate`; PostgreSQL via Testcontainers |
| `messaging-kafka` | Spring Kafka producer and consumer; Kafka (`cp-kafka` 7.4.4) via Testcontainers |
| `validation-cache-scheduling` | `@Validated` method validation, Caffeine `@Cacheable`, `@Scheduled` job tested with Awaitility |

## Layout

Each app directory has one source tree and three build entry points:

```
apps/<app>/
  pom.xml                      Maven build (no parent POM, see below)
  src/main/{java,kotlin,resources}
  src/test/{java,kotlin,resources}
  gradle-groovy/               build.gradle, settings.gradle, gradle.properties
  gradle-kotlin/               build.gradle.kts, settings.gradle.kts, gradle.properties
```

Gradle cannot have `build.gradle` and `build.gradle.kts` in the same directory, so each Gradle DSL lives in its
own subdirectory. Those builds set their source sets to `../src/...`. A Gradle build therefore needs the
parent directory (`apps/<app>`) to be present, which is why `docker/gradle.sh` mounts the parent when it finds
`../src`.

Maven does not use `spring-boot-starter-parent`. It imports `spring-boot-dependencies` as a BOM with
`${boot.version}`, and sets the `spring-boot-maven-plugin` version to the same property. The Testcontainers
apps also import `testcontainers-bom`, since Boot 2.7.18 does not manage Testcontainers.

## Java and Kotlin levels

- Java: source and target 1.8 in all builds. Compile with JDK 17 and avoid APIs newer than Java 8.
- Kotlin (`webflux-kotlin-coroutines`): Kotlin 1.6.21, `jvmTarget` 1.8, `kotlinx-coroutines` 1.6.4.

## Switching the Boot version (T-7.2)

The Boot version is set in one place per build:

| Build | Where | Override without editing |
|-------|-------|--------------------------|
| Maven | `<boot.version>` in `pom.xml` (default `2.7.18`) | `-Dboot.version=2.7.18.1-SNAPSHOT` |
| Gradle Groovy | `bootVersion` in `gradle-groovy/gradle.properties` | `-PbootVersion=2.7.18.1-SNAPSHOT` |
| Gradle Kotlin | `bootVersion` in `gradle-kotlin/gradle.properties` | `-PbootVersion=2.7.18.1-SNAPSHOT` |

The Gradle value drives both the Spring Boot plugin (`pluginManagement` in the settings file) and the BOM.
The fork is installed in the local Maven repository before the builds run (T-7.2 builds it with `docker/build.sh`).

## Matrix plan for T-7.2

Every app is built and tested with each combination below:

- Boot: original `2.7.18` and fork `2.7.18.1-SNAPSHOT`
- JDK: 8, 11, 17
- Build: Maven, Gradle Groovy, Gradle Kotlin

That is 2 x 3 x 3 = 18 combinations per app, 108 in total. JDK 8 and 11 use the existing `docker/build.sh`
and `docker/mvn.sh` images; `docker/gradle.sh` currently accepts only 11 and 17.

## Running in Docker

Builds run in containers only. The Testcontainers apps (`data-jpa-flyway`, `messaging-kafka`) need the Docker
socket, so set `LTS_DOCKER_SOCKET=1`. Without Docker, their tests are skipped by `@Testcontainers(disabledWithoutDocker = true)`.

```sh
docker/mvn.sh repos/lts-compat-tests/apps/web-mvc-actuator 17 -q verify
LTS_DOCKER_SOCKET=1 docker/mvn.sh repos/lts-compat-tests/apps/data-jpa-flyway 17 -q verify
docker/gradle.sh repos/lts-compat-tests/apps/web-mvc-actuator/gradle-kotlin 17 test
```

## Version pins that matter

- Testcontainers `1.21.4`: 1.17 sends a Docker API version that current Docker Engine rejects.
- `confluentinc/cp-kafka:7.4.4` and `postgres:14-alpine`: both have arm64 images.
- Spring Security 5.7 API (`antMatcher`, `antMatchers`): `securityMatcher` and `requestMatchers(String...)` are 5.8 APIs.
