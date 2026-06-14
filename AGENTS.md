# Agent Conventions & Guidance

This file provides guidance for AI agents (Claude Code, Junie, etc.) when working with this repository.

---

## Quick Start

**Stack:** Java 25, Spring Boot 4.0.6, Spring Data JDBC, PostgreSQL, Liquibase, MapStruct, Lombok

**Prerequisites:** The `budget-buddy-contracts` dependency is fetched from GitHub Packages. Set these before building:

```bash
export GITHUB_ACTOR=your-github-username
export GITHUB_TOKEN=your-personal-access-token   # needs read:packages scope
```

Or add `gpr.user` / `gpr.key` to `~/.gradle/gradle.properties`.

**Run locally:**

```bash
# Automatically starts PostgreSQL via Docker Compose (dev profile)
./gradlew bootRun --args='--spring.profiles.active=dev'
```

**Test:** (the `test` suite is unit-only; integration tests run via `integrationTest` and require Docker)

```bash
./gradlew test                                          # unit tests
./gradlew integrationTest                               # integration tests (requires Docker)
./gradlew check                                         # unit + integration tests + JaCoCo coverage report
./gradlew test --tests "com.budget.buddy.budget_buddy_api.category.CategoryServiceTest"          # single unit test
./gradlew integrationTest --tests "com.budget.buddy.budget_buddy_api.category.CategoryIntegrationTest"  # single integration test
```

See [TESTING.md](TESTING.md) for how to write tests — naming & structure, unit vs. integration policy, and the coverage
that's mandatory before a change is done (notably auth + cross-user ownership-isolation tests for every ownable
endpoint).

**Build:**

```bash
./gradlew build
```

**Pre-PR verification checklist** — run these before opening a pull request:

1. **Tests pass**
   ```bash
   ./gradlew test integrationTest
   ```
2. **No open Sonar issues** — if the `MCP_DOCKER` MCP server is available, query it directly:
   ```
   Tool: mcp__MCP_DOCKER__search_sonar_issues_in_projects
   Args: { "projects": ["budget-buddy-org_budget-buddy-api"], "issueStatuses": ["OPEN"] }
   ```
   Resolve any `OPEN` issues before merging. Project key: `budget-buddy-org_budget-buddy-api`.

---

## Agent Conventions

### Error Handling

- **RFC 9457 (Problem Details)**: All error responses must follow the Problem Details for HTTP APIs specification.
- **Standardized Titles**: When `type` is `about:blank`, the `title` SHOULD be the same as the HTTP status phrase (
  e.g., "Bad Request" for 400).
- **Field-Level Errors**: Validation exceptions (`MethodArgumentNotValidException`, `ConstraintViolationException`) must
  return a `Problem` containing an `errors` array with `field` and `message` properties.
- **Request URI**: The `instance` field should contain the current request URI, retrieved using `ServletWebRequest` in
  `GlobalExceptionHandler`.

### Logging

- **Always use `@Slf4j`**: Declare loggers via Lombok `@Slf4j` on the class — never use `LoggerFactory.getLogger()` manually.
- **Log levels**:
  - `ERROR` — unexpected failures that need immediate attention; always include the exception as a second argument (`log.error("...", ex)`).
  - `WARN` — recoverable issues the system can handle but that need visibility: auth failures, access-denied, data integrity violations.
  - `INFO` — significant business events: entity created (`id=…`), entity deleted (`id=…`). Visible in all environments.
  - `DEBUG` — request flow detail: reads, updates, list/count calls, validation steps. Off in production by default.
  - `TRACE` — fine-grained internals; reserved for framework-level diagnostics.
- **No PII in logs**: Never log OIDC subjects, raw JWTs, or personal data at any level. Internal UUIDs (our own generated IDs) are safe to log. Use DEBUG for anything that might be user-identifying.
- **No full-object logging**: Never pass a full request/response/entity to a log statement — log only IDs and counts to avoid inadvertently leaking user data.
- **MDC correlation**: Every request carries two MDC keys populated automatically:
  - `requestId` — set by `RequestCorrelationFilter` (propagated from `X-Request-ID` header or generated); echoed back in the response header.
  - `userId` — set by `OidcUserProvisioningFilter` after JWT authentication resolves to a local user UUID.
  - Both keys appear in every log line. Do not clear or overwrite them; let the filters manage the lifecycle.
- **Dev vs prod output**: Dev uses Spring Boot's default human-readable console pattern extended with `[requestId] [userId]`. Prod emits structured JSON (ECS format via `logging.structured.format.console: ecs`) — all MDC keys appear as top-level fields automatically.
- **Parameterized messages**: Always use SLF4J placeholders (`log.debug("id={}", id)`) — never string concatenation.

### Security & OIDC

- **Stateless resource server**: The API validates JWTs from an external OIDC provider. No sessions, no server-side
  token storage.
- **JIT user provisioning**: `OidcUserProvisioningFilter` runs after `BearerTokenAuthenticationFilter` and maps JWT
  `sub` + `iss` claims to a local user via an atomic upsert (`UserService.findOrCreateByOidcSubject`, cached). It then
  replaces the `JwtAuthenticationToken` on the `SecurityContext` with a `LocalUserAuthentication` carrying the resolved
  local user UUID — the UUID lives in the security context, **not** a request attribute, so it propagates to any
  context-aware thread.
- **Multi-issuer support**: Users are identified by the composite `(oidc_subject, oidc_issuer)` unique constraint. The
  same `sub` from different issuers creates separate users.
- **Audience validation**: JWTs must contain an expected audience configured via `OIDC_AUDIENCES`.
- **Ownership isolation**: All ownable entities are scoped to the authenticated user via `OwnerIdProvider<UUID>` (
  `OidcOwnerIdProvider`), which reads the local user UUID from the `LocalUserAuthentication` on the
  `SecurityContextHolder`. `OwnableEntityService` applies this owner id to every query and stamps it on create —
  subclasses inherit isolation with no extra wiring.
- **No PII in logs**: Never log OIDC subjects or other user-identifying claims at INFO level or above. Use DEBUG.

### Code Patterns

- **Problem Details Extension**: The base `Problem` class from `budget-buddy-contracts` includes an `errors` field for
  field-level validation errors. Use it directly instead of extending it for common validation cases.
- **Validation Handling**:
    - Bean Validation failures on request bodies/params surface as `MethodArgumentNotValidException` /
      `ConstraintViolationException` → field-level `errors[]`. Prefer `ex.getBindingResult().getFieldErrors()` and
      `ex.getConstraintViolations()` respectively.
    - **Domain rules** that need lookups or context (e.g. "category must exist and belong to the caller" in
      `TransactionValidator`) throw `ValidationException` (`base.exception`), mapped to **400** by
      `GlobalExceptionHandler`. Use it instead of `IllegalArgumentException` so business-rule failures are distinct from
      programming errors. The message is surfaced to the client as `detail`, so keep it free of internal state.
- **Transactional Methods**:
    - **Read-Only Operations**: All service-level read operations (e.g., `read`, `list`, `count`) MUST be marked with
      `@Transactional(readOnly = true)`.
    - **Class-Level Default**: Prefer setting `@Transactional(readOnly = true)` at the class level and overriding it
      with `@Transactional` on specific write methods (create, update, delete).

### Feature Architecture (CRUDL framework)

Domain features (`category`, `transaction`) are **package-by-feature** and extend a shared generic CRUDL hierarchy in
`base/crudl/`. The contract is:

- **Entity** — extend `OwnableEntity<UUID>` (inherits `id`, `version`, `createdAt`, `updatedAt` from `BaseEntity` and
  adds `ownerId` for per-user isolation). Identity is assigned by `BaseEntityListener` on insert.
- **Repository** — extend `OwnableEntityRepository<E, UUID>` (gives `findByIdAndOwnerId`, `findAllByOwnerId`,
  `existsByIdAndOwnerId`, `countByOwnerId`). Add derived-query methods as needed.
- **Service** — extend `OwnableEntityService<E, UUID, R, C, U>`. All CRUDL operations are auto-scoped to the current
  owner; you inherit isolation with no extra code. When a subclass needs the concrete repository/mapper type, **override
  the getter covariantly** rather than re-declaring a field:
  ```java

@Override
private TransactionRepository getRepository() { return (TransactionRepository) super.getRepository(); }

```
- **Controller** — extend `BaseEntityController<…>` **and** `implements <Domain>Api` (the generated interface). Delegate
  to the `*Internal` helpers (`createInternal`, `readInternal`, …) and **always override `createdURI()`**. Build
  pagination envelopes with the inherited `toMeta(Page)` helper.
- **Mapper** — see MapStruct conventions below.
- **Validator** *(optional)* — implement `BaseEntityValidator<E>` as a `@Component`; it's auto-collected into the
  service's validator set and run before every save.

### API-First / Contracts

`budget-buddy-contracts` is the **single source of truth**. Controllers implement generated Spring interfaces and all
request/response DTOs (`Transaction`, `TransactionWrite`, `Problem`, `PaginationMeta`, …) come from the generated
package — **never hand-write or edit them here**. To change the API surface: update the OpenAPI spec in
`budget-buddy-contracts` first, publish a new version, bump `budgetBuddyContractsVersion` in `build.gradle.kts`, then
implement the regenerated interfaces.

- **PUT is full replacement; there is no PATCH.** `updateEntity` overwrites every writable field, nulls included.
  Partial-update (PATCH) endpoints are intentionally not offered — do not reintroduce them.

### MapStruct Mappers

- Annotate mappers with `@Mapper(config = MapstructConfig.class)` — the shared config sets the Spring component model
  and constructor injection. Extend `BaseEntityMapper<E, R, C, U, L>`.
- **Never map onto immutable/identity fields**: `id`, `version`, `createdAt`, `updatedAt`, `ownerId` are `@Mapping(...,
  ignore = true)` on `toEntity`/`updateEntity` (the base interface already declares these — keep them when overriding).
- `toModel` / `toModelList` map entity → contract DTO; `toPageResponse(items, meta)` builds the paginated envelope.

### Persistence & Entities

- **Spring Data JDBC, not JPA.** Aggregates are mutable POJOs: `@Table` + `@Id` + `@Column`, with Lombok
  `@Getter @Setter @NoArgsConstructor @AllArgsConstructor`. Column names are string literals in `@Column` annotations —
  use Java property names (e.g. `"createdAt"`) in `Sort.by(...)` and let Spring Data JDBC map them to column names.
- **Optimistic locking** via the `@Version` field inherited from `BaseEntity`; **auditing**
  (`createdAt`/`updatedAt`) is automatic — never set these by hand.
- **Custom type conversion** lives in `CustomJdbcConverters` (e.g. `Currency`, Postgres enums, timestamp→
  `OffsetDateTime`).
  Register new converters there.

### Database Migrations (Liquibase)

- Numbered SQL files under `src/main/resources/db/changelog/migrations/` (`NNN-short-description.sql`), registered in
  `db.changelog-master.yaml`.
- Each file starts with `--liquibase formatted sql` and a `--changeset <author-email>:<NNN-id>` header.
- **Every changeset must include a `--rollback`.** **Never edit an already-applied changeset** — add a new one.

### Configuration Properties

Bind external config with a `@Validated @ConfigurationProperties` **record** (constructor binding), e.g.
`CorsProperties`.
Profiles: `application.yaml` (shared) + `application-{dev,prod,test}.yaml`. Secrets/issuer come from env vars.

### Null-Safety (JSpecify)

Every production package is `@NullMarked` (via a `package-info.java`), so references are **non-null by default**;
annotate only the genuinely nullable ones with `org.jspecify.annotations.@Nullable` (e.g.
`CategoryEntity.monthlyBudget`). **Do not write `@NonNull`** — it's redundant in a null-marked scope. Prefer JSpecify
over `jakarta`/Spring nullability annotations.

These annotations are **documentation + tooling hints**, not build-enforced: IntelliJ (and other JSpecify-aware tools)
reads `@NullMarked`/`@Nullable` and flags mismatches in the editor. There is intentionally **no NullAway / Error Prone
build gate** — keep the build simple and rely on the IDE plus the conventions below.

- **Adding a new package?** Add a `package-info.java` declaring `@NullMarked` so the package opts into non-null-by-default.
- **Framework-initialised fields**: Spring Data JDBC populates `@Column`-mapped entity fields after construction, so a
  no-arg constructor legitimately leaves them unset — that's expected and needs no annotation gymnastics.

### Raw SQL Repositories (`JdbcClient`)

When a query is too dynamic or aggregate-heavy for Spring Data JDBC, drop down to `JdbcClient` with text-block SQL.
Examples: `TransactionSummaryRepository`, `CategorySummaryRepository`. Conventions:

- **Named parameters as constants**: lift each `:bindingName` into a `private static final String` constant. The
  constant is used in both the SQL literal and the `.param(NAME, value)` call so renames stay in sync.
- **Extract `RowMapper`s**: declare each `RowMapper<T>` as a `private static final` field rather than inline lambdas.
  Reuse mappers when one row type is built from another (e.g. a bucketed mapper delegating to a per-row mapper via
  `mapRow`).
- **Result alias literals**: SELECT-list aliases (e.g. `AS income_count`) stay literal — they're query-local artifacts,
  not part of the persistent schema.
- **Stream over list**: prefer `.query(mapper).stream().collect(...)` over `.list().stream()` to avoid materializing an
  intermediate list when collecting to a different shape (e.g. `Map`).
- **Return value objects**: repositories return small `record`s (e.g. `TransactionSummaryRow`,
  `TransactionTrendBucket`), not API DTOs. The service layer maps to contract types and handles cross-row concerns like
  zero-filling missing buckets.
