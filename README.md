# Quarkus Quotation Service

This repository contains a small Quarkus-based service that fetches USD-BRL currency prices from an external REST API, persists selected quotations into a PostgreSQL database using Panache, and emits events to Kafka when a new higher quotation price is detected.

## Quick status / what has been implemented

- REST client
  - `org.stef.client.CurrencyPriceClient` — a MicroProfile Rest Client (`@RegisterRestClient`) that calls `https://economia.awesomeapi.com.br/last/{pair}`.
- DTOs
  - `org.stef.dto.CurrencyPriceDTO` — root DTO containing `USDBRL`.
  - `org.stef.dto.USDBRL` — fields returned by the external API (bid, pctChange, etc.).
  - `org.stef.dto.QuotationDTO` — minimal payload emitted to Kafka (date + currencyPrice).
- Persistence
  - `org.stef.entity.QuotationEntity` — JPA entity mapped to table `quotation`.
  - `org.stef.repository.QuotationRepository` — Panache repository for `QuotationEntity`.
  - `src/main/resources/import.sql` — placeholder import file for dev/test.
- Business logic
  - `org.stef.service.QuotationService` — fetches the latest price, compares it with the last stored record, persists a new record if the price increased, and triggers Kafka events.
- Messaging
  - `org.stef.message.KafkaEvents` — an `Emitter<QuotationDTO>` bound to channel `quotation-channel` (`@Channel("quotation-channel")`) which sends events to Kafka.
- Configuration
  - `src/main/resources/application.properties` — contains database, REST client and a few Kafka/messaging properties.
- Build
  - `pom.xml` — Quarkus BOM, dependencies include: `quarkus-hibernate-orm-panache`, `quarkus-jdbc-postgresql`, `quarkus-rest-client-jackson`, `quarkus-jackson`, and `quarkus-messaging-kafka`.
- Docker
  - Dockerfiles present in `src/main/docker/` for different packaging modes (`Dockerfile.jvm`, `Dockerfile.native*`, `Dockerfile.legacy-jar`).

## Project structure (important files)

- src/main/java/org/stef/client/CurrencyPriceClient.java — MicroProfile Rest Client interface
- src/main/java/org/stef/dto/* — DTO classes (CurrencyPriceDTO, USDBRL, QuotationDTO)
- src/main/java/org/stef/entity/QuotationEntity.java — JPA entity
- src/main/java/org/stef/repository/QuotationRepository.java — Panache repository
- src/main/java/org/stef/service/QuotationService.java — business logic, persistence and event emission
- src/main/java/org/stef/message/KafkaEvents.java — SmallRye Reactive Messaging Emitter wrapper
- src/main/resources/application.properties — environment configuration
- src/main/resources/import.sql — dev/test data import (commented template)

## How it works (runtime flow)

1. `QuotationService.getCurrencyPrice()` calls the remote service through `CurrencyPriceClient.getPriceByPair("USD-BRL")`.
2. The service converts the returned `CurrencyPriceDTO` to a BigDecimal price and compares it to the last stored `QuotationEntity`.
3. If there is no previous record, or the new price is greater than the last recorded price, it persists a new `QuotationEntity` and emits a `QuotationDTO` to the `quotation-channel` using `KafkaEvents.sendNewKafkaEvent(...)`.
4. `KafkaEvents` holds an `Emitter<QuotationDTO>` tied to the `quotation-channel` and sends the payload downstream to the Kafka connector.

## How to build and run

- Development mode (fast feedback):

```powershell
./mvnw quarkus:dev
```

- Package an executable jar (fast):

```powershell
./mvnw package -DskipTests
java -jar target/quarkus-app/quarkus-run.jar
```

- Docker (example for JVM Dockerfile):

```powershell
docker build -f src/main/docker/Dockerfile.jvm -t quotation-service:jvm .
docker run -e QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://host.docker.internal:5432/quotationdb -e QUARKUS_DATASOURCE_USERNAME=postgres -e QUARKUS_DATASOURCE_PASSWORD=1234 -p 8080:8080 quotation-service:jvm
```

## How to test the Kafka path locally (quick checklist)

- Start a local Kafka broker (for example using Docker / Confluent images or `bitnami/kafka`).
- Ensure the `quotation` topic exists or let Kafka auto-create it (depending on broker settings).
- Start the Quarkus app in dev mode and trigger `QuotationService.getCurrencyPrice()` (you can add a quick REST endpoint or call it from a unit test / scheduler).
- Observe logs and use `kafka-console-consumer` to verify messages.

## Known limitations & next steps

- Serialization: current code sends `QuotationDTO` objects. Confirm the outgoing channel serializer or send JSON strings explicitly.
- Error handling: network errors from the external API are not handled (no retries/backoff).
- Scheduling: there is a `scheduler` package present but currently empty — add a `@Scheduled` job to call `getCurrencyPrice()` periodically.
- Consumer: there is only an emitter (producer side). Add a consumer implementation if you need internal processing of the topic.
- Tests: add unit and integration tests (Quarkus `@QuarkusTest`) to validate persistence and messaging.

