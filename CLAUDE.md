# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

Early-stage scaffold. Only the Spring Boot entry point (`WorklogapiApplication`) and a context-load test exist. `README.md` documents the *planned* REST API (accounts, JWT login with refresh token, work-log posts of max 140 characters, weekly summaries) — those endpoints are not implemented yet. Planned datastore is PostgreSQL, but no database/JPA/security dependencies are in `build.gradle` yet.

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
