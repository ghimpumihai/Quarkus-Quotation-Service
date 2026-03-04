# 💱 Quarkus Quotation Service

An event-driven microservice built with [Quarkus](https://quarkus.io/) that continuously monitors the **USD → BRL** currency exchange rate. The service polls an external REST API, persists new all-time-high quotations to a PostgreSQL database, and broadcasts price-increase events to an Apache Kafka topic for downstream consumers.

---

## 📑 Table of Contents

- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Core Workflow](#-core-workflow)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
    - [Prerequisites](#prerequisites)
    - [1. Provision Infrastructure](#1-provision-infrastructure)
    - [2. Run in Development Mode](#2-run-in-development-mode)
    - [3. Build for Production](#3-build-for-production)
- [Configuration](#-configuration)
- [Kafka Events](#-kafka-events)
- [API Reference](#-api-reference)
- [Infrastructure Services](#-infrastructure-services)

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     Quarkus Application                         │
│                                                                 │
│  ┌─────────────────┐    ┌──────────────────┐                   │
│  │  Quartz         │───▶│  CurrencyPrice   │                   │
│  │  Scheduler      │    │  Client          │──▶ AwesomeAPI     │
│  │  (every 35s)    │    │  (REST Client)   │    (External)     │
│  └─────────────────┘    └──────────────────┘                   │
│           │                      │                             │
│           ▼                      ▼                             │
│  ┌──────────────────────────────────────┐                      │
│  │         QuotationService             │                      │
│  │   (High-water mark business logic)   │                      │
│  └──────────────────────────────────────┘                      │
│           │                      │                             │
│           ▼                      ▼                             │
│  ┌─────────────────┐    ┌──────────────────┐                   │
│  │  PostgreSQL     │    │  Kafka Producer  │──▶ `quotation`    │
│  │  (Panache ORM)  │    │  (SmallRye)      │    topic          │
│  └─────────────────┘    └──────────────────┘                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🧰 Tech Stack

| Layer | Technology |
|---|---|
| Framework | [Quarkus](https://quarkus.io/) (Java) |
| Scheduling | [Quartz](https://quarkus.io/guides/quartz) |
| External API | [MicroProfile REST Client](https://quarkus.io/guides/rest-client) — AwesomeAPI |
| Persistence | PostgreSQL + [Hibernate ORM with Panache](https://quarkus.io/guides/hibernate-orm-panache) |
| Messaging | [Apache Kafka](https://kafka.apache.org/) via [SmallRye Reactive Messaging](https://smallrye.io/smallrye-reactive-messaging/) |
| Infrastructure | Docker & Docker Compose |
| Java Version | JDK 25 |

---

## ⚙️ Core Workflow

The service implements a **high-water mark** strategy — only price increases are recorded and propagated.

```
Every 35 seconds
      │
      ▼
[1] Poll AwesomeAPI for latest USD-BRL bid price
      │
      ▼
[2] Query DB for the most recent stored quotation
      │
      ▼
[3] Is new price > last stored price? (or DB is empty?)
      │
     YES ──▶ [4] Persist new Quotation entity to PostgreSQL
                          │
                          ▼
                 [5] Emit QuotationDTO to
                     `quotation` Kafka topic
      │
      NO ──▶ Discard — no action taken
```

| Step | Component | Responsibility |
|---|---|---|
| 1 | `QuotationScheduler` | Triggers the pipeline every 35 seconds via Quartz |
| 2 | `CurrencyPriceClient` | Fetches the current `bid` rate from the AwesomeAPI |
| 3 | `QuotationService` | Compares new price against the latest DB record |
| 4 | `Quotation` (Panache Entity) | Persists the new high-water mark to PostgreSQL |
| 5 | SmallRye Reactive Messaging | Publishes a `QuotationDTO` event to the `quotation` Kafka topic |

---

## 📁 Project Structure

```
src/
└── main/
    ├── java/
    │   └── .../
    │       ├── client/
    │       │   └── CurrencyPriceClient.java   # MicroProfile REST Client
    │       ├── dto/
    │       │   └── QuotationDTO.java          # Kafka event payload
    │       ├── entity/
    │       │   └── Quotation.java             # Panache entity
    │       ├── scheduler/
    │       │   └── QuotationScheduler.java    # Quartz job definition
    │       └── service/
    │           └── QuotationService.java      # Core business logic
    └── resources/
        └── application.properties             # App & infra configuration
docker-compose.yaml                            # Local infrastructure
```

---

## 🚀 Getting Started

### Prerequisites

- **JDK 25** — required by the compiler release property
- **Docker** and **Docker Compose** — for local infrastructure

### 1. Provision Infrastructure

Start all required services (PostgreSQL, Kafka, and the Kafka UI) with a single command:

```bash
docker-compose up -d
```

This will spin up:

| Service | Description | Port |
|---|---|---|
| PostgreSQL | Application database | `5432` |
| Apache Kafka (KRaft) | Message broker | `9092` |
| Conduktor Console | Kafka management UI | `9080` |

Verify all containers are healthy before proceeding:

```bash
docker-compose ps
```

### 2. Run in Development Mode

Quarkus Dev Mode provides live coding, automatic restart on file changes, and the [Dev UI](http://localhost:8080/q/dev):

```bash
./mvnw quarkus:dev
```

The application will start at **http://localhost:8080**.

> **Tip:** In Dev Mode, Quarkus can automatically provision PostgreSQL and Kafka containers via **Dev Services** — check `application.properties` to see if this is configured, in which case step 1 may be optional for local development.

### 3. Build for Production

**Standard JVM build:**

```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

**Native executable (GraalVM required):**

```bash
./mvnw package -Pnative
./target/*-runner
```

---

## 🔧 Configuration

Key properties in `src/main/resources/application.properties`:

```properties
# DataSource
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=<user>
quarkus.datasource.password=<password>
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/<db>

# Hibernate ORM
quarkus.hibernate-orm.database.generation=update

# Kafka
kafka.bootstrap.servers=localhost:9092
mp.messaging.outgoing.quotation.connector=smallrye-kafka
mp.messaging.outgoing.quotation.topic=quotation
mp.messaging.outgoing.quotation.value.serializer=...

# External API
quarkus.rest-client.awesome-api.url=https://economia.awesomeapi.com.br
```

> Update credentials and URLs to match your environment.

---

## 📨 Kafka Events

When a new high-water mark is detected, the service publishes a message to the **`quotation`** topic.

**Topic:** `quotation`  
**Payload — `QuotationDTO`:**

```json
{
  "id": 42,
  "bid": "5.4231",
  "timestamp": "2025-01-15T10:30:00"
}
```

> Field names may vary based on your `QuotationDTO` implementation. The topic can be inspected via the **Conduktor Console** at [http://localhost:9080](http://localhost:9080).

---

## 🌐 API Reference

**External data source:** [AwesomeAPI](https://docs.awesomeapi.com.br/)

The service calls the following endpoint:

```
GET https://economia.awesomeapi.com.br/json/last/USD-BRL
```

**Sample response (abbreviated):**

```json
{
  "USDBRL": {
    "bid": "5.4231",
    "ask": "5.4300",
    "high": "5.4500",
    "low": "5.4100",
    "timestamp": "1736938200"
  }
}
```

---

## 🐳 Infrastructure Services

| Service | Image | Port | Purpose |
|---|---|---|---|
| `postgres` | `postgres:latest` | `5432` | Primary application database |
| `kafka` | KRaft-mode Kafka | `9092` | Message broker |
| `conduktor-console` | `conduktor/conduktor-platform` | `9080` | Kafka management & monitoring UI |

To stop and remove all containers:

```bash
docker-compose down
```

To also remove persisted volumes:

```bash
docker-compose down -v
```

---
