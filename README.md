# medz-gql-api

![Version](https://img.shields.io/badge/version-0.0.1--SNAPSHOT-blue)
![Java](https://img.shields.io/badge/Java-25-orange)
![Spring_Boot](https://img.shields.io/badge/Spring_Boot-4.0.5-6DB33F)
![GraphQL](https://img.shields.io/badge/GraphQL-Spring_for_GraphQL-E10098)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-required-4169E1)

`medz-gql-api` is a Spring Boot GraphQL service for querying Algerian medicine regulatory data. It exposes a single GraphQL schema for medicines, their current status, and lifecycle events such as nomenclature updates, withdrawals, and non-renewals.

## Why this project is useful

- Query medicine records by registration number, code, INN/ICD, brand name, or laboratory holder
- Search across multiple fields with optional origin, status, and laboratory-holder filters
- Explore regulatory history through a GraphQL union of medicine events
- Avoid common N+1 GraphQL issues with batched status and event resolution
- Keep business logic isolated through a hexagonal architecture with clear domain ports and adapters
- Observe local runs with OpenTelemetry and the included Grafana LGTM stack

## Tech stack

- Java 25
- Spring Boot 4.0.5
- Spring for GraphQL
- Spring Data JPA + PostgreSQL
- MapStruct + Lombok
- OpenTelemetry + Grafana LGTM

## Architecture at a glance

The project follows a hexagonal architecture:

- `application/` — GraphQL controllers and application configuration
- `domain/` — framework-free ports, immutable records, and services
- `infrastructure/` — JPA entities, repositories, mappers, and persistence adapters

Typical request flow:

`MedicineController` → `MedicineApi` → `MedicineService` → `MedicineSpi` → `MedicineAdapter` → JPA repositories → PostgreSQL

For more implementation details, see [`AGENTS.md`](AGENTS.md).

## Getting started

### Prerequisites

You will need:

- JDK 25
- Docker and Docker Compose (optional, for observability tooling)
- A local PostgreSQL instance listening on `localhost:5432`
- A database named `medz`
- Credentials matching the default Spring configuration:
  - username: `postgres`
  - password: `password`

> The repository currently targets Java 25 in [`pom.xml`](pom.xml). A clean Maven build will fail on older JDKs.

### Clone and prepare the project

```bash
git clone git@github.com:anisbouhadida/medz-gql-api.git
cd medz-gql-api
```

### Configure the database

By default, the application reads these settings from [`src/main/resources/application.yaml`](src/main/resources/application.yaml):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/medz
    username: postgres
    password: password
```

You can either keep these defaults or override them with standard Spring environment variables before running the app:

```bash
export SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5432/medz'
export SPRING_DATASOURCE_USERNAME='postgres'
export SPRING_DATASOURCE_PASSWORD='password'
```

### Start optional observability tooling

The included [`compose.yaml`](compose.yaml) starts Grafana LGTM locally:

```bash
docker compose up -d
```

This exposes Grafana LGTM on `http://localhost:3000` and OTLP ports for local tracing. PostgreSQL is **not** provisioned by `compose.yaml`.

### Build and test

```bash
./mvnw clean verify
```

### Run the API

```bash
./mvnw spring-boot:run
```

Once the application is running, the main endpoints are:

- GraphQL endpoint: `http://localhost:8080/graphql`
- GraphiQL UI: `http://localhost:8080/graphiql`

## Usage examples

The GraphQL schema lives in [`src/main/resources/graphql/schema.graphqls`](src/main/resources/graphql/schema.graphqls).

### Fetch one medicine by registration number

```graphql
query {
  medicineByRegistrationNumber(registrationNumber: "12345") {
    id
    registrationNumber
    code
    internationalCommonDenomination
    brandName
    status
    event {
      __typename
      ... on NomenclatureEvent {
        finalRegistrationDate
        stabilityDuration
      }
      ... on WithdrawalEvent {
        withdrawalDate
        withdrawalReason
      }
      ... on NonRenewalEvent {
        finalRegistrationDate
        observations
      }
    }
  }
}
```

### Search medicines with filters

```graphql
query {
  medicineSearch(
    filter: {
      searchText: "paracetamol"
      origin: IMPORTED
      status: ACTIVE
      laboratoryHolders: ["SAIDAL"]
    }
  ) {
    id
    registrationNumber
    brandName
    origin
    status
  }
}
```

### Retrieve events directly

```graphql
query {
  medicineEvents(registrationNumber: "12345") {
    __typename
    ... on WithdrawalEvent {
      eventType
      status
      withdrawalDate
      withdrawalReason
    }
  }
}
```

## Project structure

```text
src/main/java/dz/anisbouhadida/medzgqlapi/
├── application/
│   ├── config/
│   └── controller/
├── domain/
│   ├── api/
│   ├── model/
│   ├── service/
│   └── spi/
└── infrastructure/
    ├── adapter/
    ├── entity/
    ├── mapper/
    └── repository/
```

A few useful entry points:

- [`src/main/resources/graphql/schema.graphqls`](src/main/resources/graphql/schema.graphqls) — GraphQL schema
- [`src/main/java/dz/anisbouhadida/medzgqlapi/application/controller/MedicineController.java`](src/main/java/dz/anisbouhadida/medzgqlapi/application/controller/MedicineController.java) — GraphQL queries and batch mappings
- [`src/main/java/dz/anisbouhadida/medzgqlapi/domain/api/MedicineApi.java`](src/main/java/dz/anisbouhadida/medzgqlapi/domain/api/MedicineApi.java) — inbound domain port
- [`src/main/java/dz/anisbouhadida/medzgqlapi/domain/spi/MedicineSpi.java`](src/main/java/dz/anisbouhadida/medzgqlapi/domain/spi/MedicineSpi.java) — outbound persistence port
- [`src/main/java/dz/anisbouhadida/medzgqlapi/infrastructure/mapper/MedicineMapper.java`](src/main/java/dz/anisbouhadida/medzgqlapi/infrastructure/mapper/MedicineMapper.java) — entity-to-domain mapping

## Where to get help

- Review the GraphQL contract in [`src/main/resources/graphql/schema.graphqls`](src/main/resources/graphql/schema.graphqls)
- Read the project-specific architecture notes in [`AGENTS.md`](AGENTS.md)
- Check Spring-generated reference pointers in [`HELP.md`](HELP.md)
- Open a repository issue: <https://github.com/anisbouhadida/medz-gql-api/issues>
- Spring references:
  - <https://docs.spring.io/spring-boot/4.0.5/reference/web/spring-graphql.html>
  - <https://docs.spring.io/spring-boot/4.0.5/reference/data/sql.html#data.sql.jpa-and-spring-data>

## Maintainers and contributing

This repository is maintained by [Anis Bouhadida](https://github.com/anisbouhadida).

Contributions are welcome through issues and pull requests. Before opening a PR:

1. Read [`CONTRIBUTING.md`](CONTRIBUTING.md)
2. Review and follow [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md)
3. Keep the hexagonal boundaries intact
4. Update the GraphQL schema whenever the API contract changes
5. Run the Maven build locally with JDK 25

## Additional notes

- `MedicineEvent` is modeled as a GraphQL union backed by sealed Java domain types.
- `DateTime` is provided through `graphql-java-extended-scalars` and registered in [`GraphQlConfig`](src/main/java/dz/anisbouhadida/medzgqlapi/application/config/GraphQlConfig.java).
- Bean validation is enforced on GraphQL inputs and translated to friendly GraphQL errors by [`GraphQlExceptionHandler`](src/main/java/dz/anisbouhadida/medzgqlapi/application/config/GraphQlExceptionHandler.java).

