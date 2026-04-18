# Contributing to medz-gql-api

Thanks for considering a contribution.

## Before you start

Make sure your local environment matches the project requirements:

- JDK 25
- PostgreSQL running locally on `localhost:5432`
- Database name: `medz`
- Default credentials: `postgres` / `password`

See [`README.md`](README.md) for setup and run commands.

## Development workflow

```bash
./mvnw clean verify
./mvnw spring-boot:run
```

If you use the observability stack locally:

```bash
docker compose up -d
```

## Architecture expectations

Please follow the project conventions documented in [`AGENTS.md`](AGENTS.md):

- Keep `domain/` free of framework and JPA annotations
- Add new use-cases to both `MedicineApi` and `MedicineSpi`, then delegate in `MedicineService`
- Update [`src/main/resources/graphql/schema.graphqls`](src/main/resources/graphql/schema.graphqls) when changing the GraphQL contract
- When adding a new `MedicineEvent` type, update the domain model, entity, GraphQL type, and GraphQL union together
- Use `MedicineMapper` for entity-to-domain transformations

## Pull requests

When opening a pull request, please:

1. Describe the problem and the change clearly
2. Mention any schema or database implications
3. Include tests or explain why tests were not added
4. Keep changes focused and avoid unrelated refactors

## Need help?

- Open an issue: <https://github.com/anisbouhadida/medz-gql-api/issues>
- Start with [`README.md`](README.md) and [`HELP.md`](HELP.md)

