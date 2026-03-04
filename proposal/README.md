# 📋 Quarkus Proposal Service

An event-driven microservice built with [Quarkus](https://quarkus.io/) that manages commercial proposals. The service exposes a REST API to create, retrieve, and delete proposals, persists them to a PostgreSQL database, and publishes proposal events to an Apache Kafka topic for downstream consumers such as the Quotation Service.

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
│  ┌─────────────────┐                                           │
│  │  REST Client    │◀── HTTP Request                           │
│  │  (JAX-RS)       │                                           │
│  └─────────────────┘                                           │
│           │                                                     │
│           ▼                                                     │
│  ┌──────────────────────────────────────┐                      │
│  │         ProposalService              │                      │
│  │   (Business logic & orchestration)   │                      │
│  └──────────────────────────────────────┘                      │
│           │                      │                             │
│           ▼                      ▼                             │
│  ┌─────────────────┐    ┌──────────────────┐                   │
│  │  PostgreSQL     │    │  Kafka Producer  │──▶ `proposal`     │
│  │  (Panache ORM)  │    │  (SmallRye)      │    topic          │
│  └─────────────────┘    └──────────────────┘                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🧰 Tech Stack

| Layer | Technology |
|---|---|
| Framework | [Quarkus](https://quarkus.io/) (Java) |
| REST API | [JAX-RS](https://quarkus.io/guides/rest) |
| Persistence | PostgreSQL + [Hibernate ORM with Panache](https://quarkus.io/guides/hibernate-orm-panache) |
| Messaging | [Apache Kafka](https://kafka.apache.org/) via [SmallRye Reactive Messaging](https://smallrye.io/smallrye-reactive-messaging/) |
| Infrastructure | Docker & Docker Compose |
| Java Version | JDK 21 |

---

## ⚙️ Core Workflow

The service receives a proposal via REST, persists it, and notifies downstream services via Kafka.

```
POST /api/proposal
      │
      ▼
[1] Deserialize ProposalDetailsDTO from request body
      │
      ▼
[2] Build Proposal entity and persist to PostgreSQL
      │
      ▼
[3] Map Proposal to ProposalDTO
      │
      ▼
[4] Emit ProposalDTO to `proposal` Kafka topic
```

| Step | Component | Responsibility |
|---|---|---|
| 1 | `ProposalController` | Receives and validates the HTTP request |
| 2 | `ProposalServiceImpl` | Builds entity, persists via Panache repository |
| 3 | `ProposalServiceImpl` | Maps persisted entity back to DTO |
| 4 | `KafkaEvent` | Publishes `ProposalDTO` event to Kafka |

---

## 📁 Project Structure

```
src/
└── main/
    ├── java/
    │   └── org/stef/
    │       ├── controller/
    │       │   └── ProposalController.java    # REST endpoints
    │       ├── dto/
    │       │   ├── ProposalDTO.java           # Lightweight Kafka payload
    │       │   └── ProposalDetailsDTO.java    # Full proposal DTO
    │       ├── entity/
    │       │   └── Proposal.java             # Panache JPA entity
    │       ├── message/
    │       │   └── KafkaEvent.java           # Kafka producer
    │       ├── repository/
    │       │   └── ProposalRepository.java   # Panache repository
    │       └── service/
    │           ├── ProposalService.java      # Service interface
    │           └── ProposalServiceImpl.java  # Business logic
    └── resources/
        └── application.properties            # App & infra configuration
```

---

## 🚀 Getting Started

### Prerequisites

- **JDK 21** — required by the compiler release property
- **Docker** and **Docker Compose** — for local infrastructure

### 1. Provision Infrastructure

Start all required services (PostgreSQL and Kafka) with a single command:

```bash
docker-compose up -d
```

This will spin up:

| Service | Description | Port |
|---|---|---|
| PostgreSQL | Application database | `5434` |
| Apache Kafka (KRaft) | Message broker | `9092` |
| Conduktor Console | Kafka management UI | `9080` |

Verify all containers are healthy before proceeding:

```bash
docker-compose ps
```

### 2. Run in Development Mode

Quarkus Dev Mode provides live coding, automatic restart on file changes, and the [Dev UI](http://localhost:8091/q/dev):

```bash
./mvnw quarkus:dev
```

The application will start at **http://localhost:8091**.

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
# HTTP
quarkus.http.port=8091

# DataSource
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=<user>
quarkus.datasource.password=<password>
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5434/proposal_db

# Hibernate ORM
quarkus.hibernate-orm.schema-management.strategy=update

# Kafka
kafka.bootstrap.servers=127.0.0.1:9092
quarkus.devservices.enabled=false

# Messaging
mp.messaging.outgoing.proposal-channel.connector=smallrye-kafka
mp.messaging.outgoing.proposal-channel.topic=proposal
mp.messaging.outgoing.proposal-channel.value.serializer=io.quarkus.kafka.client.serialization.ObjectMapperSerializer
```

> Update credentials and URLs to match your environment.

---

## 📨 Kafka Events

When a proposal is successfully created, the service publishes a message to the **`proposal`** topic.

**Topic:** `proposal`  
**Payload — `ProposalDTO`:**

```json
{
  "proposalId": 1,
  "customer": "Acme Corp",
  "priceTonne": 150.00
}
```

> The topic can be inspected via the **Conduktor Console** at [http://localhost:9080](http://localhost:9080).

---

## 🌐 API Reference

Base path: `/api/proposal`

| Method | Path | Description |
|---|---|---|
| GET | `/{id}` | Retrieve full proposal by ID |
| POST | `/` | Create a new proposal |
| DELETE | `/{id}` | Delete a proposal by ID |

**POST `/api/proposal` — Request Body:**

```json
{
  "customer": "Acme Corp",
  "priceTonne": 150.00,
  "tonnes": 10,
  "country": "Romania",
  "proposalValidityDays": 30
}
```

**Responses:**

| Status | Description |
|---|---|
| `200 OK` | Operation successful |
| `500 Internal Server Error` | Unexpected error, message included in body |

---

## 🐳 Infrastructure Services

| Service | Image | Port | Purpose |
|---|---|---|---|
| `postgres` | `postgres:latest` | `5434` | Primary application database |
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