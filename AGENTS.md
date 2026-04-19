# AGENTS.md – medz-gql-api

GraphQL API for Algerian medicine regulatory data. Built with Spring Boot 4, Java 25, Spring for GraphQL, JPA/PostgreSQL, MapStruct, and Lombok.

## Architecture

Hexagonal (ports & adapters) with three layers:

```
application/          → primary adapters (GraphQL controllers, config)
domain/               → pure business logic; no framework deps
  api/                → inbound ports (MedicineApi)
  model/              → immutable Java records (Medicine, MedicineEvent subtypes...)
  spi/                → outbound ports (MedicineSpi)
  service/            → MedicineService implements MedicineApi, depends on MedicineSpi
infrastructure/       → secondary adapters
  entity/             → JPA @Entity classes
  repository/         → Spring Data JPA repositories
  mapper/             → MapStruct mappers (Entity → domain record)
  adapter/            → MedicineAdapter implements MedicineSpi
```

**Data flow:** `MedicineController` → `MedicineApi` (domain port) → `MedicineService` → `MedicineSpi` (infra port) → `MedicineAdapter` → JPA repositories → PostgreSQL.

## Key Patterns

- **Domain models are Java records** (immutable). Never add JPA or framework annotations to `domain/model/`.
- **`MedicineApi` mirrors `MedicineSpi`** almost 1-to-1; `MedicineService` delegates straight through. Add new query use-cases to both interfaces and `MedicineService`.
- **`MedicineEvent` is a sealed domain union** (`NomenclatureEvent | WithdrawalEvent | NonRenewalEvent`), mapped to a GraphQL `union`. New event types require: new domain record, new entity, new repository, `MedicineMapper` method, `MedicineAdapter` wiring, new GraphQL type, and a `union` update in `schema.graphqls`. The current event subtype entities are `@OneToOne`/`@MapsId` tables keyed by `medicine_id`, and `MedicineAdapter.findEventsByMedicineIds(...)` builds the GraphQL list by concatenating the three subtype repositories.
- **MapStruct** converts entities to domain records in `MedicineMapper`. Field renames use `@Mapping` (e.g. `medicineId→id`, `icd→internationalCommonDenomination`). Lombok + MapStruct require the specific annotation processor ordering in `pom.xml`.
- **Infrastructure enums use PostgreSQL named enums** in JPA entities (`@Enumerated(EnumType.STRING)` + `@JdbcTypeCode(SqlTypes.NAMED_ENUM)` in `MedicineEntity`, `MedicineStatusHistoryEntity`, and `MedicineEventHistoryEntity`). Keep Java enum constant names aligned with the database enum labels.
- **`@BatchMapping`** is used in `MedicineController` for `status` and `event` fields on `Medicine` to avoid N+1 queries.
- **`Medicine.status` and `Medicine.event` are resolver-backed GraphQL fields**; `domain/model/Medicine.java` intentionally does not contain them. Keep them in `schema.graphqls` + `MedicineController` batch resolvers instead of pushing them into the core record.
- **`scalar DateTime`** is registered via `ExtendedScalars.DateTime` in `GraphQlConfig`. Any new custom scalar must be similarly wired.
- **Bean validation** on GraphQL inputs: `@Validated` on the controller + JSR-380 constraints on `@Argument` params and input records (see `MedicineSearchFilter`, which also rejects empty filter objects via `@AssertTrue`).
- **`MedicineAdapter` stays read-only transactional** (`@Transactional(readOnly = true)` at class level); keep new query paths inside that pattern so lazy JPA associations remain accessible during mapping.
- **Search is implemented with JPA `Specification`s** in `MedicineAdapter.search(...)`; free-text matching covers `registrationNumber`, `code`, `icd`, and `brandName`, escapes SQL `LIKE` wildcards, status filtering goes through `MedicineStatusHistoryRepository.findMedicineIdsByCurrentStatus(...)`, and `laboratoryHolders` filtering is a lower-cased exact `IN (...)` match rather than another `LIKE` search.

## Developer Workflows

```bash
# Build & test
./mvnw clean verify

# Run (requires PostgreSQL on localhost:5432, db=medz, user=postgres, password=password)
./mvnw spring-boot:run

# GraphiQL UI (dev only, enabled in application.yaml)
open http://localhost:8080/graphiql

# Docker Compose starts Grafana LGTM (OpenTelemetry + observability)
docker compose up -d
```

- PostgreSQL is **not** managed by Docker Compose – run it separately.
- Local Maven commands require a real **JDK 25** toolchain; older JDKs fail compilation with `release version 25 not supported`.
- Virtual threads are enabled (`spring.threads.virtual.enabled: true`); avoid `ThreadLocal`-based patterns.
- OpenTelemetry is included via `spring-boot-starter-opentelemetry`; traces export to the LGTM stack from `compose.yaml`.
- Test coverage is currently minimal: `src/test/java/dz/anisbouhadida/medzgqlapi/MedzGqlApiApplicationTests.java` is a single `@SpringBootTest` smoke test, and there is no separate `src/test/resources` test configuration.

## Key Files

| File | Purpose |
|------|---------|
| `src/main/resources/graphql/schema.graphqls` | Single source of truth for the GraphQL schema |
| `application/controller/MedicineController.java` | GraphQL query mappings plus batched resolvers for `Medicine.status` and `Medicine.event` |
| `domain/api/MedicineApi.java` | Inbound port – add new use-cases here first |
| `domain/spi/MedicineSpi.java` | Outbound port – mirror changes from MedicineApi |
| `infrastructure/adapter/MedicineAdapter.java` | Read-only JPA adapter; search/specification logic and event/status aggregation live here |
| `infrastructure/mapper/MedicineMapper.java` | Entity→domain mapping; use `@Mapping` for field renames |
| `infrastructure/repository/MedicineStatusHistoryRepository.java` | JPQL queries for current/latest status lookups used by search and batch loading |
| `src/test/java/dz/anisbouhadida/medzgqlapi/MedzGqlApiApplicationTests.java` | Current automated test coverage: a single Spring context smoke test |
| `application/config/GraphQlConfig.java` | Custom scalar registration |
| `application/config/GraphQlExceptionHandler.java` | Centralized GraphQL error handling |
| `src/main/resources/application.yaml` | Local datasource defaults, GraphiQL enablement, and virtual-thread configuration |

