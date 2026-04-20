# medz-gql-api

![Version](https://img.shields.io/badge/version-0.0.1--SNAPSHOT-blue)
![Java](https://img.shields.io/badge/Java-25-orange)
![Spring_Boot](https://img.shields.io/badge/Spring_Boot-4.0.5-6DB33F)
![GraphQL](https://img.shields.io/badge/GraphQL-Spring_for_GraphQL-E10098)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-required-4169E1)

`medz-gql-api` is a Spring Boot GraphQL service for querying Algerian medicine regulatory data. It exposes a single GraphQL schema for medicines, their current status, and lifecycle events such as nomenclature updates, withdrawals, and non-renewals.

## Why this project is useful

- Query medicine records by registration number, code, INN/ICD, brand name, or laboratory holder
- Search across multiple fields with optional origin, status, and laboratory-holder filters, with cursor-based pagination and multi-field sorting
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

### Search medicines with filters and pagination

```graphql
query {
  medicinesSearch(
    filter: {
      searchText: "paracetamol"
      origin: IMPORTED
      status: ACTIVE
      laboratoryHolders: ["SAIDAL"]
    }
    first: 10
    sort: [{ field: BRAND_NAME, direction: ASC }]
  ) {
    totalCount
    pageInfo { hasNextPage endCursor }
    edges {
      cursor
      node {
        id
        registrationNumber
        brandName
        origin
        status
      }
    }
  }
}
```

### Retrieve events directly

Events are resolver-backed fields on each `Medicine` node. Request them alongside the medicine inside any query:

```graphql
query {
  medicineByRegistrationNumber(registrationNumber: "12345") {
    registrationNumber
    status
    event {
      __typename
      ... on WithdrawalEvent {
        eventType
        status
        withdrawalDate
        withdrawalReason
      }
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

## Pagination and sorting

The `medicinesSearch` query implements **Relay-spec cursor-based pagination** and **multi-field sorting**. 

---

### Tutorial — your first paginated search

This walkthrough shows how to page through active imported medicines, two at a time, from first page to last.

**Step 1 — fetch the first page**

```graphql
query {
  medicinesSearch(
    filter: { status: ACTIVE, origin: IMPORTED }
    first: 2
  ) {
    totalCount
    pageInfo {
      hasNextPage
      endCursor
    }
    edges {
      cursor
      node {
        id
        registrationNumber
        brandName
      }
    }
  }
}
```

You receive something like:

```json
{
  "data": {
    "medicinesSearch": {
      "totalCount": 47,
      "pageInfo": {
        "hasNextPage": true,
        "endCursor": "b2Zmc2V0OjE="
      },
      "edges": [
        { "cursor": "b2Zmc2V0OjA=", "node": { "id": "1", "registrationNumber": "REG-001", "brandName": "Doliprane" } },
        { "cursor": "b2Zmc2V0OjE=", "node": { "id": "2", "registrationNumber": "REG-002", "brandName": "Clamoxyl" } }
      ]
    }
  }
}
```

**Step 2 — fetch the next page**

Copy the `endCursor` value from `pageInfo` and pass it as `after`:

```graphql
query {
  medicinesSearch(
    filter: { status: ACTIVE, origin: IMPORTED }
    first: 2
    after: "b2Zmc2V0OjE="
  ) {
    pageInfo { hasNextPage endCursor }
    edges { cursor node { id registrationNumber } }
  }
}
```

Keep repeating this pattern until `hasNextPage` is `false`.

**Step 3 — go back one page**

To navigate backward, use `last` and `before` with the `startCursor` you received from `pageInfo`:

```graphql
query {
  medicinesSearch(
    filter: { status: ACTIVE, origin: IMPORTED }
    last: 2
    before: "b2Zmc2V0OjI="
  ) {
    pageInfo { hasPreviousPage startCursor }
    edges { cursor node { id registrationNumber } }
  }
}
```

**Step 4 — add sorting**

Pass a `sort` list to order results. Multiple criteria are applied in order:

```graphql
query {
  medicinesSearch(
    filter: { searchText: "paracetamol" }
    first: 10
    sort: [
      { field: LABORATORY_HOLDER, direction: ASC }
      { field: BRAND_NAME, direction: DESC }
    ]
  ) {
    edges { node { id brandName laboratoryHolder } }
  }
}
```

### Reference

#### Pagination arguments on `medicinesSearch`

| Argument | Type | Default | Constraint | Description |
|----------|------|---------|-----------|-------------|
| `first` | `Int` | `20` | 1–100 | Number of edges to return in the forward direction |
| `after` | `String` | — | valid cursor | Return edges **after** this cursor (exclusive) |
| `last` | `Int` | `20` | 1–100 | Number of edges to return in the backward direction |
| `before` | `String` | — | valid cursor | Return edges **before** this cursor (exclusive) |
| `sort` | `[MedicineSortInput!]` | `[{REGISTRATION_NUMBER, ASC}]` | — | Ordered list of sort criteria |

`first`/`after` and `last`/`before` are mutually exclusive. Mixing them produces a `BAD_REQUEST` validation error.

#### `MedicineSortInput` fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `field` | `MedicineSortField!` | Yes | The field to sort by |
| `direction` | `SortDirection!` | Yes (default `ASC`) | Sort direction |

#### `MedicineSortField` enum values

| Value | Database column |
|-------|----------------|
| `REGISTRATION_NUMBER` | `registration_number` |
| `BRAND_NAME` | `brand_name` |
| `LABORATORY_HOLDER` | `laboratory_holder` |
| `INITIAL_REGISTRATION_DATE` | `initial_registration_date` |

#### `MedicineConnection` response type

| Field | Type | Description |
|-------|------|-------------|
| `edges` | `[MedicineEdge!]!` | The page's edges, each containing a `node` and a `cursor` |
| `pageInfo` | `PageInfo!` | Pagination state for the current page |
| `totalCount` | `Int!` | Total number of matching medicines across all pages |

#### `PageInfo` fields

| Field | Type | Description |
|-------|------|-------------|
| `hasNextPage` | `Boolean!` | `true` when more edges exist after `endCursor` |
| `hasPreviousPage` | `Boolean!` | `true` when more edges exist before `startCursor` |
| `startCursor` | `String` | Cursor of the first edge; `null` when `edges` is empty |
| `endCursor` | `String` | Cursor of the last edge; `null` when `edges` is empty |

#### Cursor format

Cursors are **opaque base64 strings**. Their internal format (`offset:<n>`) is an implementation detail and must not be relied upon. Always treat cursors as black boxes sourced from a previous API response.

---

### Explanation — how pagination works under the hood

#### Why cursor-based pagination?

Offset-based pagination (`LIMIT x OFFSET y`) is simple but breaks silently under concurrent writes: inserting a row before page 2 while a client is paging shifts all subsequent rows, causing items to be skipped or duplicated. Cursor-based pagination anchors each page to a stable position in the result set, making it safe for datasets that change between requests.

The GraphQL community standardised this pattern as the **Relay Connection Specification**, which this API follows. The envelope types (`MedicineConnection`, `MedicineEdge`, `PageInfo`) are the direct result of that spec.

#### Cursor encoding

Each cursor encodes the **absolute 0-based row offset** of its edge within the sorted result set:

```
raw  = "offset:<n>"          e.g. "offset:40"
wire = base64(raw)            e.g. "b2Zmc2V0OjQw"
```

This encoding is handled by `CursorUtils` in the application layer. The adapter decodes the cursor back to its numeric offset and derives the correct Spring Data `PageRequest` page number using integer division (`offset ÷ pageSize`).

#### Forward vs. backward pagination

- **Forward** (`first` / `after`): the cursor offset `afterOffset` is decoded, the page starts at `afterOffset + 1`, and `pageNumber = startOffset / pageSize`.
- **Backward** (`last` / `before`): the cursor offset `beforeOffset` is decoded, the page ends just before that position, so `startOffset = max(0, beforeOffset − pageSize)` and `pageNumber = startOffset / pageSize`.

Both directions produce a standard Spring Data `PageRequest`, so the same JPA `Specification` query handles both transparently.

#### Where each concern lives (hexagonal architecture)

The pagination feature is layered cleanly across the three hexagonal rings:

| Layer | Type / Class | Responsibility |
|-------|-------------|----------------|
| **Domain** | `MedicinePageRequest` | Carries the client's raw pagination and sort intent; validated with `@AssertTrue` |
| **Domain** | `MedicineSortInput`, `MedicineSortField`, `SortDirection` | Framework-free sort vocabulary |
| **Domain** | `MedicinePage` | Neutral pagination result envelope (content + metadata); no GraphQL or JPA types |
| **Domain port** | `MedicineApi` / `MedicineSpi` | `search(filter, pageRequest) → MedicinePage` |
| **Infrastructure** | `MedicineAdapter` | Decodes cursors, builds `PageRequest` + `Sort`, calls `findAll(spec, pageable)`, returns `MedicinePage` |
| **Application** | `CursorUtils` | Encodes row offsets as opaque base64 cursors for the GraphQL response |
| **Application** | `MedicineConnection`, `MedicineEdge`, `PageInfo` | GraphQL-specific Relay envelope records; built from `MedicinePage` in the controller |
| **Application** | `MedicineController` | Maps GraphQL arguments → `MedicinePageRequest`, calls the domain port, maps `MedicinePage` → `MedicineConnection` |

The domain knows nothing about GraphQL cursors; it works entirely with offsets and plain lists. The infrastructure knows nothing about Relay connection types; it works entirely with Spring Data pages. Only the application layer bridges the two vocabularies.

#### Sorting and the default order

Sort criteria flow from the GraphQL `sort` argument → `MedicineSortInput` records in the domain → `Sort.Order` list in `MedicineAdapter.buildSort()`. Because cursor-based pagination requires a **stable and deterministic sort**, the default (`REGISTRATION_NUMBER ASC`) is deliberately chosen as the primary key of the logical result set. If you override sorting, make sure the sort key is unique (or add `REGISTRATION_NUMBER` as a tiebreaker) to avoid non-deterministic page boundaries.

#### `totalCount` and its cost

Every call to `medicinesSearch` fires two queries: the paginated `SELECT` for the current page, and a `COUNT(*)` for `totalCount`. For large datasets with complex filter specifications this count query can become expensive. If `totalCount` is not needed by the client, omit it from the selection set — Spring for GraphQL resolves only the fields actually requested.

---

## Additional notes

- `MedicineEvent` is modeled as a GraphQL union backed by sealed Java domain types.
- `DateTime` is provided through `graphql-java-extended-scalars` and registered in [`GraphQlConfig`](src/main/java/dz/anisbouhadida/medzgqlapi/application/config/GraphQlConfig.java).
- Bean validation is enforced on GraphQL inputs and translated to friendly GraphQL errors by [`GraphQlExceptionHandler`](src/main/java/dz/anisbouhadida/medzgqlapi/application/config/GraphQlExceptionHandler.java).

