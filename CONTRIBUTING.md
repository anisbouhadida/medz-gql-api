# Contributing to medz-gql-api

Thanks for considering a contribution.

## Before you start

Make sure your local environment matches the project requirements:

- JDK 25
- PostgreSQL running locally on `localhost:5432`
- Database name: `medz`
- Default credentials: `postgres` / `password`

See [`README.md`](README.md) for setup and run commands.

Please also review and follow [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md) in all project spaces, including issues and pull requests.

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
3. Use the GitHub pull request templates in `.github/PULL_REQUEST_TEMPLATE/` and complete the relevant sections
4. Include steps to reproduce or validate the change, especially for bug fixes and user-facing behavior
5. Summarize any security impact at a high level, but do **not** disclose sensitive vulnerability details publicly; use [`SECURITY.md`](SECURITY.md) for private reporting when needed
6. Include tests run, or explain why tests were not added
7. Confirm whether documentation, GraphQL schema docs, or migration notes were updated
8. Keep changes focused and avoid unrelated refactors

## Issues

When opening a GitHub issue, please:

1. Use the templates in `.github/ISSUE_TEMPLATE/`
2. For bugs, include exact reproduction steps, expected vs. actual behavior, and environment details
3. For feature requests, describe the problem, proposed solution, concrete use cases, and success criteria
4. For support/questions, describe your goal, what you already tried, and the specific help you need
5. Link related issues with `#number` references when applicable
6. Attach screenshots, logs, or stack traces when they help explain the issue
7. Do **not** report suspected security vulnerabilities in a public issue; follow [`SECURITY.md`](SECURITY.md) instead

## Need help?

- Open an issue: <https://github.com/anisbouhadida/medz-gql-api/issues>
- Start with [`README.md`](README.md) and [`HELP.md`](HELP.md)

