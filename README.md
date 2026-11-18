# Budget Buddy API

[![CI](https://github.com/budget-buddy-org/budget-buddy-api/actions/workflows/ci.yaml/badge.svg)](https://github.com/budget-buddy-org/budget-buddy-api/actions/workflows/ci.yaml)
[![Release](https://img.shields.io/github/v/release/budget-buddy-org/budget-buddy-api)](https://github.com/budget-buddy-org/budget-buddy-api/releases)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=budget-buddy-org_budget-buddy-api&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=budget-buddy-org_budget-buddy-api)

REST API for personal budget management. Built with Spring Boot 4 and PostgreSQL.

## Tech Stack

- **Java 25** / Spring Boot 4
- **Spring Data JDBC** + PostgreSQL
- **Spring Security** — stateless OIDC resource server with JWT validation
- **Liquibase** — database migrations
- **Budget Buddy Contracts** — external OpenAPI-based contracts
- **Testcontainers** — integration tests
- **Docker** — containerized deployment

## Prerequisites

- Java 25
- Docker & Docker Compose
- GitHub Packages authentication (see [Building](#building))

## Running Locally

The project uses Spring Docker Compose — PostgreSQL starts automatically when you run the app.

```bash
# Clone the repository
git clone https://github.com/budget-buddy-org/budget-buddy-api.git
cd budget-buddy-api

# Set OIDC configuration (required for JWT validation)
export OIDC_ISSUER_URI=https://<your-auth-server-host>
export OIDC_AUDIENCES=https://<your-oidc-audiences>

# Run with dev profile
./gradlew bootRun --args='--spring.profiles.active=dev'
```

The API will be available at `http://localhost:8080`.

## Running with Docker Compose

```bash
# Copy and fill in environment variables
cp .env.example .env

# Start all services
docker compose up -d

# View logs
docker compose logs -f app
```

### Environment Variables

| Variable                 | Description                                                            |
|--------------------------|------------------------------------------------------------------------|
| `BUDGET_BUDDY_API_IMAGE` | Docker image (e.g. `ghcr.io/username/budget-buddy-api:latest`)         |
| `SPRING_PROFILES_ACTIVE` | Spring profile (`prod`)                                                |
| `DB_NAME`                | PostgreSQL database name                                               |
| `DB_USER`                | PostgreSQL username                                                    |
| `DB_PASSWORD`            | PostgreSQL password                                                    |
| `OIDC_ISSUER_URI`        | OIDC issuer URI for JWT validation (e.g. `https://<auth-server-host>`) |
| `OIDC_AUDIENCES`         | Expected JWT audience(s) (e.g. `https://<your-oidc-audiences>`)        |

## API

### Authentication

Authentication is handled by an external OIDC provider. The API is a stateless resource server that validates JWTs using the JWKS endpoint discovered from `OIDC_ISSUER_URI`.

- **Issuer & audience validation** — JWTs are verified against the configured issuer and must contain an expected audience (`OIDC_AUDIENCES`).
- **Multi-issuer support** — The same `sub` claim from different issuers is treated as separate users (composite unique constraint on `oidc_subject + oidc_issuer`).
- **JIT provisioning** — On first authenticated request, a local user is automatically created by mapping the JWT `sub` and `iss` claims to a local user record.

### Categories

| Method   | Endpoint                      | Description                      |
|----------|-------------------------------|----------------------------------|
| `GET`    | `/v1/categories`              | List categories                  |
| `POST`   | `/v1/categories`              | Create category                  |
| `GET`    | `/v1/categories/{categoryId}` | Get category                     |
| `PUT`    | `/v1/categories/{categoryId}` | Replace category (full update)   |
| `DELETE` | `/v1/categories/{categoryId}` | Delete category                  |

### Transactions

| Method   | Endpoint                           | Description                         |
|----------|------------------------------------|-------------------------------------|
| `GET`    | `/v1/transactions`                 | List transactions (with filters)    |
| `POST`   | `/v1/transactions`                 | Create transaction                  |
| `GET`    | `/v1/transactions/{transactionId}` | Get transaction                     |
| `PUT`    | `/v1/transactions/{transactionId}` | Replace transaction (full update)   |
| `DELETE` | `/v1/transactions/{transactionId}` | Delete transaction                  |

All endpoints require `Authorization: Bearer <access_token>` (JWT issued by the OIDC provider).

## Building

To build the project, you need to provide GitHub credentials to access the `budget-buddy-contracts` package. You can set `GITHUB_ACTOR` and `GITHUB_TOKEN` environment variables, or provide them via `gradle.properties`:

```bash
# Build and run tests
./gradlew build -Pgpr.user=YOUR_USERNAME -Pgpr.key=YOUR_TOKEN
```

### Java formatting

Spotless formats all Java source sets (production, unit tests, and integration tests)
with the default Google Java Format style. It also applies the default import ordering,
removes unused imports, and replaces fully qualified type references with imports where possible.

```bash
./gradlew spotlessApply         # Format Java sources
./gradlew spotlessCheck         # Check formatting without changing files
```

Formatting is also checked automatically by `./gradlew check` and `./gradlew build`.

### Build Docker image

```bash
./gradlew bootBuildImage --imageName=ghcr.io/your-username/budget-buddy-api:latest
```

## Tests

```bash
./gradlew test                  # Run unit tests
./gradlew integrationTest       # Run integration tests (requires Docker)
./gradlew check                 # Run all tests + quality checks
```

## Health Check

```
GET /actuator/health
```

### Update behavior

`PUT` fully replaces writable fields, including clearing nullable fields when omitted
or explicitly set to `null`. PATCH endpoints are not supported.

### Error Responses (RFC 9457)

The API uses standardized Problem Details for HTTP APIs (RFC 9457) for all error responses.
When `type` is `about:blank`, the `title` field corresponds to the standard HTTP status phrase.

For validation errors (`400 Bad Request`), the response is extended with an `errors` array containing field-level details:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "One or more fields are invalid",
  "instance": "/v1/transactions",
  "errors": [
    { "field": "amount", "message": "must be greater than 0" }
  ]
}
```

### Releases

Git tags and GitHub Releases are the version and release-note sources.
Application releases do not create version-bump or changelog commits.
Production image builds receive the verified release version from shared CI.
Local builds use a development version; web preview builds use the source revision.
