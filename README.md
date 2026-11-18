# Protoge: argumentation checklist

A web app for building structured arguments. Users log in with Google, create *cases*, add *arguments* to them,
attack arguments (rebutting, undercutting, undermining) to build an argument tree, add *evaluations*, and view or
export reports (tree view, Excel).

Stack: Spring Boot 4, Java 17, Spring MVC + Thymeleaf, Spring Data JPA, Spring Security (Google OAuth2 login),
PostgreSQL, Apache POI.

## Run locally

Start PostgreSQL (database, user and password are all `petclinic`):

```bash
docker compose up postgres
```

Then run the app with the `postgres` profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

Open <http://localhost:8080/>. Configuration comes from environment variables:

| Variable | Purpose | Default |
|---|---|---|
| `POSTGRES_URL` | JDBC URL | `jdbc:postgresql://localhost/petclinic` |
| `POSTGRES_USER` | database user | `petclinic` |
| `POSTGRES_PASS` | database password | `petclinic` |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 client secret | `dummy-secret` (login will not work) |

## Database

The schema is not managed by Hibernate.

- `src/main/resources/db/postgres/schema.sql` is the full schema for a fresh database.
- Changes to an existing database are idempotent scripts in `src/main/resources/db/postgres/migrations/`,
  run manually with `psql`.

## Build and test

```bash
./mvnw spring-javaformat:apply              # fix formatting (the build fails on violations)
./mvnw package                              # build, tests and checks
./mvnw test -Dtest=AttackControllerTests    # a single test class
./mvnw package -P css                       # recompile petclinic.css from src/main/scss
```

`PostgresIntegrationTests` starts PostgreSQL through Docker Compose, so it needs Docker. For fast feedback in an IDE you can
run the `main()` methods in `PetClinicIntegrationTests` and `PostgresIntegrationTests`.

## Container

The `Dockerfile` builds the app with Maven. See [README.Docker.md](README.Docker.md) for details; Kubernetes
manifests are in [k8s/](k8s/).

```bash
docker build --platform=linux/amd64 -t checklist .
```

## More documentation

- [docs/TestCases.md](docs/TestCases.md): manual use-case scenarios.
- [CLAUDE.md](CLAUDE.md): architecture, security model and conventions.

## Origin and license

This project is a fork of [Spring PetClinic](https://github.com/spring-projects/spring-petclinic). The Java package is
still `org.springframework.samples.petclinic`. Released under the [Apache License 2.0](LICENSE.txt).
