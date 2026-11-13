# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A fork of Spring PetClinic that has been turned into an **argumentation checklist** app: users log in with Google, create *cases*, add *arguments* to them, attack arguments (rebutting / undercutting / undermining) to build an argument tree, add *evaluations*, and view/export reports (tree view, Excel). `README.md` is mostly upstream PetClinic text and is stale (e.g. H2/MySQL were removed; only PostgreSQL is supported). The Java package is still `org.springframework.samples.petclinic`, and leftover `owner`/`vet`/`pets` packages, templates and schema tables are PetClinic remnants. Manual use-case scenarios are in `docs/TestCases.md`.

Stack: Spring Boot 4.0.x, Java 17, Spring MVC + Thymeleaf, Spring Data JPA, Spring Security with OAuth2 login (Google), PostgreSQL, Caffeine cache, Apache POI.

## Commands

Maven is the only build (Dockerfile and CI use it).

```bash
./mvnw spring-boot:run                      # run (needs PostgreSQL on localhost:5432, db/user/pass = petclinic)
docker compose up postgres                  # start the database
./mvnw package                              # build + tests + checks
./mvnw test -Dtest=AttackControllerTests    # single test class
./mvnw test -Dtest=AttackControllerTests#methodName
./mvnw spring-javaformat:apply              # fix formatting (build fails on violations via the validate phase)
./mvnw package -P css                       # recompile petclinic.css from SCSS (Maven only)
```

- Formatting is enforced by `spring-javaformat` (tabs for Java/XML, see `.editorconfig`); run `spring-javaformat:apply` before building. A nohttp checkstyle check also runs (no `http://` URLs in sources).
- DB connection comes from env vars `POSTGRES_URL`, `POSTGRES_USER`, `POSTGRES_PASS`; Google OAuth secret from `GOOGLE_CLIENT_SECRET`.
- IntelliJ run config: `.run/PetClinicApplication (postgres) local.run.xml`.

## Tests

- `@WebMvcTest` slice tests (e.g. `UserAdminSecurityTests`, `UsersMenuTests`, `ArgumentDetailsPageTests`) import `SecurityConfig` and `UserLocaleResolver` explicitly and mock repositories with `@MockitoBean`. Follow this pattern for controller/security tests.
- `ClinicServiceTests` (`@DataJpaTest` with `Replace.NONE`) hits the real configured PostgreSQL, so it needs a running DB.
- `PostgresIntegrationTests` starts Postgres via Spring Boot Docker Compose support (needs Docker). `PetClinicIntegrationTests` and `I18nPropertiesSyncTest` are `@Disabled`.

## Architecture

**Domain (`cases` package)** — `Case` (table `cases`) owns a list of `Argument`s and belongs to a `User`. `Argument` is self-referential: `parentId` + `attacks` (children) form the argument tree; attacks are stored as `Argument` rows with a parent. Each argument has `Evaluation`s. `ArgumentType`, `SourceType`, `IntensityType`, `VerificationStatus` are lookup types with Thymeleaf formatters. Collections are `FetchType.EAGER`, `spring.jpa.open-in-view=false`.

**Controllers** — `CaseController`, `ArgumentsController` (`/cases/{caseId}/arguments/...`), `AttackController` (`/cases/{caseId}/arguments/{argumentId}/{rebutting|undercutting|undermining}/new`), `EvaluationController`, `report/ReportController`, `export/*` (tree JSON data, Excel export), `system/UserController` (admin user list).

**Security model** (`config/SecurityConfig`, `config/CaseSecurity`):
- Everything requires authentication except static resources, `/login`, `/error`, `/actuator/health`, `/ping`. CSRF is disabled.
- Login is OAuth2 only; `system/CustomOAuth2UserService` finds or registers a `User` by email and wraps it in `CustomUserPrincipal`. Login events are recorded via `LoginAuditService`.
- `/users/**` is restricted to users with `users.admin = true`.
- Per-case access is enforced with `@PreAuthorize("@caseSecurity.checkCaseOwner(#caseId, authentication)")` on every case-scoped handler. This only checks `caseId`; handlers that also take an `argumentId`/`evaluationId` must additionally verify that the child belongs to that case (see `AttackController.findArgumentInCase`). New case-scoped endpoints need both. `CaseEndpointsSecuredTests` scans all controllers and fails if any mapping containing `{caseId}` lacks `@PreAuthorize`, so name the path variable `caseId` for case-scoped endpoints.

**Database** — schema is not managed by Hibernate (`ddl-auto=none`) and `spring.sql.init.mode=never`. `src/main/resources/db/postgres/schema.sql` is the full schema for fresh DBs; changes to existing DBs go in `db/postgres/migrations/NNN_*.sql` as idempotent scripts run manually with `psql`. When changing the schema, update both `schema.sql` and add a migration. Flyway is a dependency but is not wired to these files. Physical naming is snake_case.

**UI / i18n** — Thymeleaf templates in `src/main/resources/templates` using `fragments/layout.html`. Messages in `messages/messages*.properties` (default, `en`, `cs`); keep keys in sync across all three. The user's language is stored on `users.language` and applied by `UserLocaleResolver`.

**Deployment** — `Dockerfile` (multi-stage Maven build), `docker-compose.yml`, `k8s/`. Actuator health probes are exposed with the DB health check disabled (deployed on AWS Lightsail).
