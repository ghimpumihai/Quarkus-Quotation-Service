# 📋 Quarkus Opportunity Service

An event-driven microservice built with [Quarkus](https://quarkus.io/) that manages commercial opportunities. The service consumes proposal and currency quotation events from Apache Kafka, correlates them into enriched opportunity records persisted in PostgreSQL, and exposes a REST API to export opportunity data as a CSV report.

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
┌──────────────────────────────────────────────────────────────────┐
│                      Quarkus Application                         │
│                                                                  │
│  ┌──────────────────┐    ┌──────────────────┐                   │
│  │  `proposal`      │    │  `quotation`     │                   │
│  │  Kafka Topic     │    │  Kafka Topic     │                   │
│  └──────────────────┘    └──────────────────┘                   │
│           │                      │                              │
│           └──────────┬───────────┘                              │
│                      ▼                                          │
│           ┌─────────────────────┐                               │
│           │     KafkaEvent      │                               │
│           │  (Message Consumer) │                               │
│           └─────────────────────┘                               │
│                      │                                          │
│                      ▼                                          │
│  ┌──────────────────────────────────────┐                       │
│  │         OpportunityService           │                       │
│  │   (Business logic & orchestration)   │                       │
│  └──────────────────────────────────────┘                       │
│           │                                                     │
│           ▼                                                     │
│  ┌─────────────────┐    ┌─────────────────────────────┐        │
│  │   PostgreSQL    │    │  REST Client (JAX-RS)        │        │
│  │  (Panache ORM)  │    │  GET /api/opportunities/     │        │
│  └─────────────────┘    │        report                │        │
│                         └─────────────────────────────┘        │
└──────────────────────────────────────────────────────────────────┘
```

---

## 🧰 Tech Stack

| Layer | Technology |
|---|---|
| Framework | [Quarkus](https://quarkus.io/) (Java) |
| REST API | [JAX-RS](https://quarkus.io/guides/rest) |
| Persistence | PostgreSQL + [Hibernate ORM with Panache](https://quarkus.io/guides/hibernate-orm-panache) |
| Messaging | [Apache Kafka](https://kafka.apache.org/) via [SmallRye Reactive Messaging](https://smallrye.io/smallrye-reactive-messaging/) |
| CSV Export | [Apache Commons CSV](https://commons.apache.org/proper/commons-csv/) |
| Infrastructure | Docker & Docker Compose |
| Java Version | JDK 21 |

---

## ⚙️ Core Workflow

The service listens on two Kafka topics — `proposal` and `quotation` — and correlates them into enriched opportunity records.

### Proposal Consumption

```
Kafka `proposal` topic
      │
      ▼
[1] Deserialize ProposalDTO from Kafka message
      │
      ▼
[2] Fetch latest Quotation from PostgreSQL
      │
      ▼
[3] Build Opportunity entity enriched with last currency quotation
      │
      ▼
[4] Persist Opportunity to PostgreSQL
```

### Quotation Consumption

```
Kafka `quotation` topic
      │
      ▼
[1] Deserialize QuotationDTO from Kafka message
      │
      ▼
[2] Persist Quotation to PostgreSQL
```

### CSV Report Generation

```
GET /api/opportunities/report
      │
      ▼
[1] Fetch all Opportunity entities from PostgreSQL
      │
      ▼
[2] Map entities to OpportunityDTOs
      │
      ▼
[3] Serialize to CSV via CSVHelper
      │
      ▼
[4] Return CSV file as octet-stream download
```

| Step | Component | Responsibility |
|---|---|---|
| Kafka consume | `KafkaEvent` | Receives and dispatches incoming Kafka messages |
| Build opportunity | `OpportunityServiceImpl` | Correlates proposal with latest quotation, persists entity |
| Save quotation | `OpportunityServiceImpl` | Persists incoming currency quotation |
| CSV export | `OpportunityServiceImpl` + `CSVHelper` | Maps entities to DTOs and serializes to CSV |

---

## 📁 Project Structure

```
src/
└── main/
    ├── java/
    │   └── org/stef/
    │       ├── controller/
    │       │   └── OpportunityController.java    # REST endpoints
    │       ├── dto/
    │       │   ├── OpportunityDTO.java           # CSV export payload
    │       │   ├── ProposalDTO.java              # Incoming Kafka proposal payload
    │       │   └── QuotationDTO.java             # Incoming Kafka quotation payload
    │       ├── entity/
    │       │   ├── Opportunity.java              # Panache JPA entity
    │       │   └── Quotation.java                # Panache JPA entity
    │       ├── message/
    │       │   └── KafkaEvent.java               # Kafka consumers
    │       ├── repository/
    │       │   ├── OpportunitiesRepository.java  # Panache repository
    │       │   └── QuotationRepository.java      # Panache repository
    │       ├── service/
    │       │   ├── OpportunityService.java       # Service interface
    │       │   └── OpportunityServiceImpl.java   # Business logic
    │       └── utils/
    │           └── CSVHelper.java                # CSV serialization utility
    └── resources/
        └── application.properties                # App & infra configuration
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

Quarkus Dev Mode provides live coding, automatic restart on file changes, and the [Dev UI](http://localhost:8092/q/dev):

```bash
./mvnw quarkus:dev
```

The application will start at **http://localhost:8092**.

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
quarkus.http.port=8092

# DataSource
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=<user>
quarkus.datasource.password=<password>
quarkus.datasource.jdbc.url=<name>

# Hibernate ORM
quarkus.hibernate-orm.schema-management.strategy=update

# Kafka
kafka.bootstrap.servers=127.0.0.1:9092
quarkus.devservices.enabled=false

# Messaging — incoming channels
mp.messaging.incoming.proposal.connector=smallrye-kafka
mp.messaging.incoming.proposal.topic=proposal
```

> Update credentials and URLs to match your environment.

---

## 📨 Kafka Events

This service acts as a **consumer** on two Kafka topics.

### Topic: `proposal`

Published by the **Proposal Service** when a new proposal is created.

**Payload — `ProposalDTO`:**

```json
{
  "proposalId": 1,
  "customer": "Acme Corp",
  "priceTonne": 150.00
}
```

### Topic: `quotation`

Published by the **Quotation Service** when a new currency rate is recorded.

**Payload — `QuotationDTO`:**

```json
{
  "date": "2024-01-15T10:30:00.000Z",
  "currencyPrice": 4.97
}
```

> Both topics can be inspected via the **Conduktor Console** at [http://localhost:9080](http://localhost:9080).

---

## 🌐 API Reference

Base path: `/api/opportunities`

| Method | Path | Description |
|---|---|---|
| GET | `/report` | Generate and download a CSV report of all opportunities |

**GET `/api/opportunities/report` — Response:**

Returns a `.csv` file download with the following columns:

| Column | Description |
|---|---|
| `proposalId` | ID of the originating proposal |
| `customer` | Customer name |
| `priceTonne` | Price per tonne from the proposal |
| `lastCurrencyQuotation` | Currency rate at time of opportunity creation |

**Responses:**

| Status | Description |
|---|---|
| `200 OK` | CSV file returned as `application/octet-stream` |
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