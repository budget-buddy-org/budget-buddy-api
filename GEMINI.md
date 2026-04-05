# Budget Buddy API - Gemini Context

This file provides context and instructions for Gemini when working on the `budget-buddy-api` project.

**For comprehensive documentation on architecture, testing, versioning, and code conventions, see [.github/SHARED.md](.github/SHARED.md).**

---

## Project Overview

**Budget Buddy API** is a RESTful service for personal budget management. It's built as a multi-tenant, secure, and auditable backend for the Budget Buddy ecosystem.

**Stack:** Java 25, Spring Boot 4.0.5, Spring Data JDBC, PostgreSQL, Liquibase, MapStruct, Lombok

**Key Modules:**
- `base/`: Core infrastructure, CRUDL framework, and global exception handling
- `user/`: User entity and service management
- `security/`: Auth controllers, JWT/Token services, and security configuration
- `category/`: Budget categories management
- `transaction/`: Financial transactions management

---

## Quick Start

### Prerequisites

- Java 25
- Docker & Docker Compose (for PostgreSQL and Testcontainers)
- GitHub Personal Access Token (for accessing `budget-buddy-contracts` from GitHub Packages)

### Key Commands

```bash
# Run locally (auto-starts PostgreSQL)
./gradlew bootRun --args='--spring.profiles.active=dev'

# Build
./gradlew build

# Testing
./gradlew test                  # Unit tests only
./gradlew integrationTest       # Integration tests (requires Docker)
./gradlew check                 # All tests and quality checks
```

---

## Gemini-Specific Guidance

### Development Workflow

When implementing features in this codebase:

1. **Understand the CRUDL pattern** — Almost all domain logic extends `OwnableEntityService` or `AbstractBaseEntityService`. Start by looking at how `CategoryService` or similar services work before implementing new features.

2. **Always update architecture before implementation** — If API contracts change, update `budget-buddy-contracts` first and bump the version in `build.gradle.kts`.

3. **Multi-tenancy is enforced** — `OwnableEntityService` automatically filters queries by `ownerId`. Never manually filter by owner in queries; rely on the framework.

4. **Leverage MapStruct for DTO mapping** — All Entity ↔ DTO conversions use MapStruct. Don't do manual mapping.

### Testing Best Practices for Gemini

Gemini works best with structured, clear test patterns:

**Use Given/When/Then structure:**
```java
@Test
void should_returnCategory_When_categoryExists() {
    // Given
    var category = categoryService.create(new CreateCategoryRequest("Food"));
    
    // When
    var result = categoryService.read(category.getId());
    
    // Then
    assertThat(result).isPresent().returns("Food", Category::getName);
}
```

**Naming conventions:**
- Test method: `should_<action>_When_<condition>()`
- Use `var` for all local variables
- Group related tests in `@Nested` classes by method or scenario

**Assertion style:**
- Use `assertThat(...).returns(expected, Type::accessor)` for multi-field checks
- Use `ArgumentCaptor` for verifying complex service interactions
- Avoid generic `any()` matchers

### Architecture Reference

For details on:
- **CRUDL framework patterns** — see [.github/SHARED.md#generic-crudl-framework](.github/SHARED.md#generic-crudl-framework)
- **API-First approach** — see [.github/SHARED.md#api-first-design](.github/SHARED.md#api-first-design)
- **Security & multi-tenancy** — see [.github/SHARED.md#security-model](.github/SHARED.md#security-model)
- **Feature addition process** — see [.github/SHARED.md#adding-a-new-feature](.github/SHARED.md#adding-a-new-feature)
- **Code conventions** — see [.github/SHARED.md#code-conventions](.github/SHARED.md#code-conventions)

---

## Links

- **Claude Code guidance:** See [CLAUDE.md](CLAUDE.md) for Claude-specific notes
- **GitHub Copilot CLI guidance:** See [.github/copilot-instructions.md](.github/copilot-instructions.md) for Copilot-specific CI/CD and deployment details
