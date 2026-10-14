# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

Early-stage scaffold. Only the Spring Boot entry point (`WorklogapiApplication`) and a context-load test exist. `README.md` documents the *planned* REST API (accounts, JWT login with refresh token, work-log posts of max 140 characters, weekly summaries) — those endpoints are not implemented yet. Planned datastore is PostgreSQL, but no database/JPA/security dependencies are in `build.gradle` yet.

When implementing a feature, treat its spec doc as the source of truth for
business rules — check it before inventing behavior, and flag if the code
needs to diverge from it.

## Stack

Java 25 (Gradle toolchain), Spring Boot 4.1.x (`spring-boot-starter-webmvc`), Gradle wrapper, JUnit Platform. Base package: `dev.bnunes.worklogapi`. Config is in `src/main/resources/application.yaml`.

## Commands

```
./gradlew bootRun                                   # run the app (port 8080)
./gradlew build                                     # compile + test
./gradlew test                                      # all tests
./gradlew test --tests dev.bnunes.worklogapi.WorklogapiApplicationTests   # single test class
./gradlew test --tests '*ClassName.methodName'      # single test method
```

No linter/formatter is configured.

## Conventions

- Planned API base path is `/worklog-api/v1/` (e.g. `/accounts`, `/auth`, `/logs`); the README curl examples define the intended request shapes.
- `.editorconfig`: LF line endings (enforced via `.gitattributes`), 4-space indent (2 for YAML), final newline. `.bat` files use CRLF.
- Commit messages use a conventional-style prefix (`doc:`, `build:`, `feat:`, `test:`, `fix:`).

### Java 25
Records for value objects, sealed interfaces, pattern matching.
No Lombok - records replace it.

### REST & Spring
Constructor injection only (no field @Autowired)
@Valid on request bodies. 201 create, 200 query, 400 validation, 404 not found.
Domain exceptions for business rule violations. Map to HTTP in controller only.
Never swallow exceptions or leak infrastructure details.


## Project Structure
domain/ - business logic, models, ports. No Spring imports.
application/ - use-case orchestration
adapter/in/web - REST controllers (Spring MVC)
adapter/out/persistence - JPA repositories and entities

NEVER import adapter classes from domain.

## Development Process - ATDD + TDD

Outer loop (ATDD):
1. Pick next acceptance criteria from the example map
2. Write one acceptance test (@SpringBootTest)
3. Inner TDD loop until the acceptance tests passes
4. Move to next AC. NEVER skip ahead.

Inner loop (TDD) - ONE TEST AT A TIME:
1. Write ONE failing test (RED)
2. Minimum code to pass (GREEN)
3. Refactor on green (REFACTOR)
4. Run ALL tests, then next test


NEVER write multiple tests before implementing.
NEVER modify a failing test. Fix the production code.


## Testing Standards
- Acceptance: .../acceptance/ - @SpringBootTest + MockMvc
- Domain: .../domain/ - plain JUnit + AssertJ, NO Spring
- Repository: .../adapter/out/persistence/ - @DataJpaTest

Domain tests: plain JUnit + AssertJ, NO Spring.
Repository tests: @DataJpaTest.
Web tests: @WebMvcTest.
Acceptance tests: @SpringBootTest + MockMvc.


## Architecture: Hexagonal (Ports & Adapters)
Domain (domain/): Pure Java. NO Spring, NO framework dependencies.
        model/ - entities and value objects 
        service/ - business rules
Application (application/): port/in/ and port/out/ interfaces.
        @Service orchestration only - no business logic here.
Adapters:
        adapter/in/web - @RestController, DTOs only.
        adapter/out/persistence - JPA repos and entities (NOT in domain layer)

Domain NEVER imports org.springframework or jakarta.persistence.
Controllers NEVER contain business logic.
Dependencies flow inward: adapter -> application -> domain


## AI Code Generation Risks
Worthless tests: Watch for tautological assertions,
assertNotNull() instead of value checks,
tests that duplicate implementation logic.

NEVER modify a test to make it pass. Fix the production code.
No @Disabled, no loosened assertions, no swallowed exceptions.

Outdated patterns: jakarta.* not javax.*. JUnit 5 only.
Verbose Java: records over POJOs, pattern matching over if/else.
Over-engineering: no interfaces with one impl (except hex ports).
Complex workarounds: if a fix feels complex, stop and ask.


## Database

Create migrations file using Flyway dependency.
If you detect that there is not a table migration for that specific feature, 
ask if you should create one.

The migrations must be located on: resources/db/migration
They must have a name starting with the version like `V1_`


