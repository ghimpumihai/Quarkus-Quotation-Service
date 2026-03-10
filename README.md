# Quarkus Quotation Service

A multi-module, event-driven backend built with **Quarkus**, **PostgreSQL**, **Kafka**, and **Keycloak**.

This project models a quotation and proposal workflow across four microservices:

- **`gateway`** — public entry point and API aggregation layer
- **`proposal`** — proposal creation, retrieval, and deletion
- **`quotation`** — exchange-rate polling and quotation event publishing
- **`report`** — opportunity aggregation and reporting

The services communicate through a mix of:

- **Synchronous REST calls** via MicroProfile REST Client
- **Asynchronous Kafka events** via SmallRye Reactive Messaging

---

## Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [How the System Works](#how-the-system-works)
- [Workflow Diagram](#workflow-diagram)
- [Services and Ports](#services-and-ports)
- [API Endpoints](#api-endpoints)
- [Authentication and Authorization](#authentication-and-authorization)
- [Running the Project](#running-the-project)
- [Build and Test](#build-and-test)
- [Kafka Topics](#kafka-topics)
- [Configuration Notes](#configuration-notes)

---

## Overview

At a high level, the platform works like this:

1. A client calls the **Gateway** API.
2. The **Gateway** forwards proposal operations to the **Proposal Service**.
3. The **Proposal Service** saves proposal data and publishes a Kafka event.
4. The **Quotation Service** periodically polls an external currency API for **USD → BRL** quotations.
5. When a new high-water mark is detected, the **Quotation Service** stores it and publishes a Kafka event.
6. The **Report Service** consumes both proposal and quotation events and builds opportunity records.
7. The **Gateway** exposes reporting endpoints that return structured JSON data or downloadable CSV content.

This architecture keeps business concerns separated while still allowing the services to collaborate.

---

## Prerequisites

Before running the project locally, make sure you have the following installed:

### Required

- **Java JDK**
  - The parent project targets **Java 25** in the root `pom.xml`
  - Some module documentation references Java 21, but the current root build config is Java 25
- **Maven** or the provided **Maven Wrapper** (`mvnw`, `mvnw.cmd`)
- **Docker**
- **Docker Compose**

### Recommended

- **IntelliJ IDEA** or another Java IDE with Maven and Quarkus support
- **Postman** or **Insomnia** for API testing
- **Kafka UI / Conduktor** for topic inspection

### Local infrastructure expected by the code

Based on the current application configuration, the services expect:

- **Kafka** at `127.0.0.1:9092`
- **Keycloak** at `http://localhost:8180/realms/quarkus`
- **PostgreSQL databases**:
  - `quotation_db` on port `5432`
  - `proposal_db` on port `5434`
  - `record_db` on port `5435`

---

## Tech Stack

| Area | Technology |
|---|---|
| Framework | Quarkus |
| Language | Java |
| Build Tool | Maven |
| Persistence | PostgreSQL + Hibernate ORM Panache |
| Messaging | Apache Kafka |
| Scheduling | Quartz |
| Security | OIDC / Keycloak |
| REST | JAX-RS + MicroProfile REST Client |
| Serialization | Jackson |
| Reporting | Apache Commons CSV |
| Testing | JUnit 5, Mockito, REST Assured |

---

## Project Structure

```text
project/
├── docker-compose.yaml              # Local infrastructure for Kafka/DB/auth services
├── pom.xml                          # Parent Maven project
├── quarkus-realm.json               # Keycloak realm import
├── README.md                        # This file
├── TEST-PLAN-IMPLEMENTATION.md      # Test strategy implementation notes
│
├── gateway/                         # Public-facing API gateway
│   ├── pom.xml
│   ├── src/
│   │   ├── main/java/org/stef/
│   │   │   ├── client/              # REST clients to downstream services
│   │   │   ├── controller/          # Gateway endpoints
│   │   │   ├── dto/                 # Shared transport DTOs
│   │   │   ├── service/             # Orchestration services
│   │   │   └── utils/               # CSV helper utilities
│   │   └── test/java/
│
├── proposal/                        # Proposal management service
│   ├── pom.xml
│   ├── src/
│   │   ├── main/java/org/stef/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── message/             # Kafka producer
│   │   │   ├── repository/
│   │   │   └── service/
│   │   └── test/java/
│
├── quotation/                       # Currency quotation polling service
│   ├── pom.xml
│   ├── src/
│   │   ├── main/java/org/stef/
│   │   │   ├── client/              # External currency API client
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── message/             # Kafka producer
│   │   │   ├── repository/
│   │   │   ├── scheduler/
│   │   │   └── service/
│   │   └── test/java/
│
└── report/                          # Opportunity aggregation and reporting service
    ├── pom.xml
    ├── src/
    │   ├── main/java/org/stef/
    │   │   ├── controller/
    │   │   ├── dto/
    │   │   ├── entity/
    │   │   ├── message/             # Kafka consumers
    │   │   ├── repository/
    │   │   └── service/
    │   └── test/java/
```

---

## How the System Works

### 1. Gateway layer

The `gateway` module is the external entry point.

It exposes APIs that:

- create and read proposals
- delete proposals
- fetch report data
- download report CSV files

Internally it forwards requests to downstream services using REST clients.

### 2. Proposal service

The `proposal` module:

- receives proposal requests
- persists proposal records in PostgreSQL
- emits a `proposal` Kafka event after successful creation

### 3. Quotation service

The `quotation` module:

- runs on a schedule
- calls an external currency API
- stores a quotation only when the new value is higher than the last stored value
- emits a `quotation` Kafka event when a new high-water mark is detected

### 4. Report service

The `report` module:

- consumes `proposal` events
- consumes `quotation` events
- joins proposal information with the latest known quotation value
- stores opportunity data
- exposes report data for the gateway to retrieve

---

## Workflow Diagram

```text
                           ┌───────────────────────┐
                           │      Client App       │
                           └───────────┬───────────┘
                                       │ HTTP
                                       ▼
                           ┌───────────────────────┐
                           │        Gateway        │
                           │      port: 8095       │
                           └──────────┬─────┬──────┘
                                      │     │
                                 REST │     │ REST
                                      ▼     ▼
                     ┌──────────────────┐  ┌──────────────────┐
                     │ Proposal Service │  │  Report Service  │
                     │    port: 8091    │  │    port: 8092    │
                     └────────┬─────────┘  └────────┬─────────┘
                              │                     ▲
                              │ Kafka: proposal     │ Kafka consumes
                              ▼                     │ proposal + quotation
                        ┌───────────────┐           │
                        │     Kafka     │───────────┘
                        └──────┬────────┘
                               │ quotation
                               ▼
                     ┌──────────────────┐
                     │ Quotation Service│
                     │   default: 8080  │
                     └────────┬─────────┘
                              │
                              │ REST polling
                              ▼
                     ┌──────────────────┐
                     │ External FX API  │
                     │    AwesomeAPI    │
                     └──────────────────┘
```

### Business flow summary

#### Proposal creation flow

```text
Client -> Gateway -> Proposal Service -> PostgreSQL
                                 |
                                 └-> Kafka topic: proposal
```

#### Quotation update flow

```text
Quartz Scheduler -> External Currency API -> Quotation Service -> PostgreSQL
                                                      |
                                                      └-> Kafka topic: quotation
```

#### Reporting flow

```text
Kafka proposal + quotation events -> Report Service -> PostgreSQL
Gateway -> Report Service -> JSON / CSV response
```

---

## Services and Ports

| Service | Purpose | Port |
|---|---|---|
| `gateway` | Public API entry point | `8095` |
| `proposal` | Proposal CRUD and Kafka producer | `8091` |
| `report` | Opportunity aggregation and reporting | `8092` |
| `quotation` | Currency polling and quotation events | Quarkus default unless overridden in config |
| `keycloak` | Authentication / realm provider | `8180` |
| `kafka` | Event broker | `9092` |

> The `quotation` module does not currently define `quarkus.http.port` in its `application.properties`, so it uses the Quarkus default unless overridden elsewhere.

---

## API Endpoints

## Gateway API

Base URL: `http://localhost:8095`

### Proposal endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/api/proposal/{id}` | Get a proposal by ID |
| POST | `/api/proposal` | Create a proposal |
| DELETE | `/api/proposal/remove/{id}` | Remove a proposal |

### Report endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/api/report` | Download CSV report |
| GET | `/api/report/data` | Get report data as JSON |

---

## Proposal Service API

Base URL: `http://localhost:8091`

| Method | Path | Description |
|---|---|---|
| GET | `/api/proposal/{id}` | Retrieve full proposal details |
| POST | `/api/proposal` | Create a new proposal |
| DELETE | `/api/proposal/{id}` | Delete a proposal |

### Example create proposal request

```json
{
  "customer": "Acme Corp",
  "priceTonne": 150.00,
  "tonnes": 10,
  "country": "Romania",
  "proposalValidityDays": 30
}
```

---

## Report Service API

Base URL: `http://localhost:8092`

| Method | Path | Description |
|---|---|---|
| GET | `/api/report/data` | Return opportunity data |

> The CSV download is exposed by the **Gateway**, not directly by the `report` module.

---

## Quotation Service API

The `quotation` module does not expose a public business endpoint in the current implementation.

Instead, it works through:

- Quartz scheduling
- external REST polling
- Kafka publication

### External API used by the quotation service

```http
GET https://economia.awesomeapi.com.br/json/last/USD-BRL
```

---

## Authentication and Authorization

The project uses **OIDC / Keycloak**.

Current configuration references:

- Realm URL: `http://localhost:8180/realms/quarkus`
- Client ID: `backend-service`
- Client secret: `secret`

### Role usage in the code

Examples of protected roles include:

- `manager`
- `user`
- `proposal-customer`

### Practical impact

- Gateway and downstream protected endpoints require a valid token
- Gateway REST clients propagate OIDC tokens to downstream services

---

## Running the Project

### 1. Start infrastructure

From the project root:

```powershell
docker-compose up -d
```

### 2. Start the services

Open one terminal per service.

#### Proposal

```powershell
cd proposal
./mvnw.cmd quarkus:dev
```

#### Report

```powershell
cd report
./mvnw.cmd quarkus:dev
```

#### Quotation

```powershell
cd quotation
./mvnw.cmd quarkus:dev
```

#### Gateway

```powershell
cd gateway
./mvnw.cmd quarkus:dev
```

### Suggested startup order

1. Infrastructure (`docker-compose`)
2. Keycloak
3. Kafka / databases
4. `proposal`
5. `report`
6. `quotation`
7. `gateway`

---

## Build and Test

### Build the whole project

From the root:

```powershell
mvn test
mvn package
```

### Build a single module

```powershell
cd gateway
./mvnw.cmd test
./mvnw.cmd package
```

### Module test commands

```powershell
cd gateway
./mvnw.cmd test

cd ..\proposal
./mvnw.cmd test

cd ..\quotation
./mvnw.cmd test

cd ..\report
./mvnw.cmd test
```

---

## Kafka Topics

| Topic | Produced By | Consumed By | Purpose |
|---|---|---|---|
| `proposal` | `proposal` service | `report` service | Notify that a proposal was created |
| `quotation` | `quotation` service | `report` service | Notify that a new quotation high-water mark was detected |

### Example payloads

#### `proposal`

```json
{
  "proposalId": 1,
  "customer": "Acme Corp",
  "priceTonne": 150.00
}
```

#### `quotation`

```json
{
  "currencyPrice": 5.42,
  "date": "2026-03-10T10:30:00Z"
}
```

---

## Configuration Notes

### Gateway

- Port: `8095`
- Talks to:
  - Proposal service at `http://localhost:8091`
  - Report service at `http://localhost:8092`

### Proposal

- Port: `8091`
- DB: `jdbc:postgresql://localhost:5434/proposal_db`
- Publishes to topic: `proposal`

### Report

- Port: `8092`
- DB: `jdbc:postgresql://localhost:5435/record_db`
- Consumes topics:
  - `proposal`
  - `quotation`

### Quotation

- DB: `jdbc:postgresql://localhost:5432/quotation_db`
- Publishes to topic: `quotation`
- Uses external currency REST API

---
