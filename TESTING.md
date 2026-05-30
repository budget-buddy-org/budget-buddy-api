# Testing Guidelines

How to write tests in this repository. These rules describe the conventions already in use — match them when adding or
changing tests.

## Source sets & how to run

| Tier            | Location                       | Base class                                       | Runs with                                  |
|-----------------|--------------------------------|--------------------------------------------------|--------------------------------------------|
| **Unit**        | `src/test/java/...`            | none (plain JUnit + Mockito)                     | `./gradlew test`                           |
| **Integration** | `src/integrationTest/java/...` | `BaseIntegrationTest` / `BaseMvcIntegrationTest` | `./gradlew integrationTest` (needs Docker) |

```bash
./gradlew test                                          # unit
./gradlew integrationTest                               # integration (Testcontainers → Docker)
./gradlew test --tests "...TransactionServiceTest"      # single class
./gradlew check                                         # everything + quality gates
```

Unit tests mirror the package of the class under test. A unit test class is named `<ClassUnderTest>Test`; an integration
test class is named `<Feature>IntegrationTest`.

## Naming & structure (both tiers)

- **Test method names:** `should_<Outcome>_When_<Condition>` (the `_When_` half is optional when there's no condition),
  e.g. `should_CreateTransaction_WithOwnerId`, `should_Return404_When_TransactionBelongsToOtherUser`.
- **Group with `@Nested`** by operation or scenario (`Create`, `Read`, `Update`, `Delete`, `List`). Keep one concern per
  nested class.
- **Use `// Given` / `// When` / `// Then` comments** to structure the body. Tiny tests may collapse `// When & Then`.
- **AssertJ only** — `assertThat(...)`, `assertThatThrownBy(...)`, `assertThatNoException()`. Never JUnit `Assertions`
  or Hamcrest.
- **Attach intent with `.as(...)`** on assertions whose failure wouldn't be self-explanatory:

```java
assertThat(entity.getOwnerId())
    .

as("Transaction owner ID should be set to the current user ID")
    .

isEqualTo(currentUserId);
```

- **Prefer `record`-style chained assertions** for multi-field checks:
  `assertThat(updated).returns(5000L, Transaction::getAmount).returns("USD", Transaction::getCurrency)`.
- **Parameterize** repetitive cases with `@ParameterizedTest` + `@EnumSource` / `@ValueSource` instead of copy-pasting.

## Unit tests

For services, validators, mappers, and other isolated logic.

- `@ExtendWith(MockitoExtension.class)`, collaborators as `@Mock`, the subject via `@InjectMocks` (or built by hand in
  `@BeforeEach` when constructor args need real values like an `OwnerIdProvider`).
- **Mock collaborators, not value objects.** Build real DTOs/entities (`new TransactionWrite()`,
  `new TransactionEntity()`); never mock them.
- **Assert state first, interactions second.** Prefer asserting the returned value or a captured argument; add
  `verify(...)` only when the interaction *is* the behavior under test (e.g. "owner id is stamped before save").
- Capture and assert arguments with `ArgumentCaptor` rather than over-specifying matchers:

```java
var filterCaptor = ArgumentCaptor.forClass(TransactionFilter.class);

verify(repository).

findAllByFilter(filterCaptor.capture(),any());

assertThat(filterCaptor.getValue().

ownerId()).

isEqualTo(currentUserId);
```

- For thrown exceptions, assert the **type and message** — and the right type. Domain validation failures throw
  `ValidationException` (see `TransactionValidator`), not `IllegalArgumentException`:

```java
assertThatThrownBy(() ->validator.

validate(entity))
    .

isInstanceOf(ValidationException .class)
    .

hasMessage("Unknown category with id: "+categoryId);
```

- Use `verifyNoInteractions(...)` / `verifyNoMoreInteractions(...)` to pin down "and nothing else happened" on the
  failure path.

## Integration tests

For wiring, security, persistence, and HTTP contract behavior. Real Postgres via Testcontainers (
`TestcontainersConfig`); each test runs in a rolled-back `@Transactional` so no manual cleanup is needed.

- **Extend the right base:** `BaseMvcIntegrationTest` for HTTP/controller tests (gives `mvc`, `json(...)`,
  `parseBody(...)`, `createTestUser()`, `jwtForUser(...)`); `BaseIntegrationTest` for repository/persistence tests.
- **Authenticate with `jwtForUser(subject)`.** Mint distinct subjects via `createTestUser()` so tests are isolated; the
  provisioning filter auto-creates the local user on first request.
- **Drive HTTP through `MockMvcTester`** and assert on the result, not by parsing unless you need the body:

```java
var result = mvc.post().uri("/v1/transactions")
    .with(jwtForUser(userId))
    .contentType(MediaType.APPLICATION_JSON)
    .content(json(new TransactionWrite().categoryId(categoryId)/* ... */))
    .exchange();

assertThat(result).hasStatus(HttpStatus.CREATED).containsHeader("Location");
var body = parseBody(result, Transaction.class);
```

- **Build fixtures through the API**, not by inserting rows directly, so tests exercise the real path (see the
  `createCategory` / `createTransaction` helpers in `TransactionIntegrationTest`).

## What must be tested

When you add or change an endpoint or domain rule, the change is not done until these exist:

- **Every endpoint:** the happy path **and** `should_Return401_When_NotAuthenticated`.
- **Every ownable endpoint** (category, transaction, and anything extending `OwnableEntityService`): a cross-user
  isolation test. Reads/updates/deletes of another user's resource must return **404** (
  `should_Return404_When_*BelongsToOtherUser`); referencing another user's resource in a write (e.g. a foreign category
  id) must return **400**. This is the load-bearing security guarantee — never skip it.
- **Every validator / business rule:** at least one failure-path test asserting the thrown type and message.
- **List/filter endpoints:** one test per filter dimension plus the empty result, ordering, and pagination-meta cases.
- **Error mapping:** when you add a `GlobalExceptionHandler` branch, add a handler test asserting the status and
  `Problem` body shape.

## Anti-patterns to avoid

- **No tautological assertions.** `assertThat(e).returns(e.getId(), BaseEntity::getId)` compares a value to itself and
  can never fail — assert against an expected literal or a separately-captured value instead.
- **No wildcard imports** (`import java.util.concurrent.*`). List imports explicitly, consistent with the rest of the
  codebase.
- **Close resources.** `ExecutorService` is `AutoCloseable` (Java 21+) — acquire it in try-with-resources rather than a
  manual `shutdownNow()` in `finally`.
- **Don't assert on mocked value objects.** A `verify` that a mock returned what you told it to return tests nothing.
- **Don't reach past the API in integration tests** to set up state the endpoint itself could create.
