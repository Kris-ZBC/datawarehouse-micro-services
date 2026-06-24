# Data Warehouse Microservices

This repository contains the database-facing microservices for the Ringsted SOP data warehouse.

The data warehouse is intended to support multiple systems, not one specific application. Its purpose is to keep general information, local mappings, and shared operational data in one consistent place instead of spreading that data across many unrelated services, applications, documents, and local databases.

The services in this project own the transactional data model and expose bounded-context APIs around that model. They communicate with the database layer through JPA and Flyway-managed schemas, and they are designed as Spring Boot microservices following Domain-Driven Design and Clean Architecture principles.

The repository also contains the saga services. Sagas belong here because they coordinate transactional workflows across bounded contexts and therefore sit close to the persistence and orchestration boundary of the system.

## Purpose

The data warehouse microservices are responsible for:

- Owning bounded-context database schemas and persistence mappings.
- Exposing internal APIs for database-backed operations.
- Coordinating multi-step workflows through saga services.
- Keeping database setup, generated DDL, Docker artifacts, and deployment scripts close to the services that depend on them.
- Providing shared libraries for security, validation, web behavior, data access, and common domain concepts.
- Creating one reliable place for shared and localized data that several systems may need.

This repository is not a frontend, public API gateway, or application-specific backend. It is an internal data warehouse service layer that stores, validates, and orchestrates shared data for Ringsted SOP-Data-IT use cases.

## Architecture

Each bounded context is structured as a separate microservice. A typical bounded context has:

- `<context>-api`: contracts, DTOs, interfaces, and shared API-level types.
- `<context>-impl`: Spring Boot implementation, JPA adapters, Flyway migrations, REST controllers, and service wiring.

Most database-aware bounded contexts expose internal endpoints under `/internal/**`. These endpoints are protected by mTLS and are intended to be called by approved sagas, handlers, or other internal services.

Saga services coordinate workflows that span multiple bounded contexts. They call the internal APIs of the database-aware services and handle orchestration at the transaction/workflow level.

Architecture diagrams are available in:

- `infra/architecture/server-microservice-architecture.puml`
- `infra/architecture/saga-microservice.architecture.puml`
- `docs/uml_diagrams/`

## Repository Layout

```text
.
|-- bc-*                         Bounded-context microservices
|-- bc-*-saga                    Saga/orchestration microservices
|-- common-core                  Shared core utilities and auto-configuration
|-- common-data                  Shared persistence/JPA support
|-- common-security              Shared security and mTLS support
|-- common-validation            Shared validation support
|-- common-web                   Shared web/API behavior
|-- shared-kernel                Shared domain concepts used across contexts
|-- master-BOM                   Central dependency and plugin versions
|-- parent-POM                   Multi-module Maven parent
|-- coverage-report              Aggregated JaCoCo reporting
|-- infra                        Database, Docker, deployment, and generation scripts
|-- docs                         Architecture and sequence documentation
|-- pom.xml                      Root Maven aggregator
|-- mvnw / mvnw.cmd              Maven wrapper
```

## Main Service Groups

Database-aware bounded contexts currently include:

- `bc-anonymize`
- `bc-apprentice`
- `bc-audit-log`
- `bc-consent`
- `bc-education`
- `bc-education-instructor`
- `bc-education-line`
- `bc-institution`
- `bc-instructor`
- `bc-login`
- `bc-message`
- `bc-message-person`
- `bc-notification`
- `bc-organisation`
- `bc-person`
- `bc-person-notification`
- `bc-sop`
- `bc-sop-education`
- `bc-sop-instructor`
- `bc-work-hour`

Saga services currently include:

- `bc-anonymize-saga`
- `bc-consent-saga`
- `bc-education-line-saga`
- `bc-education-saga`
- `bc-login-saga`
- `bc-message-saga`
- `bc-registration-saga`

## Database Access

Services use Spring Data JPA/Hibernate for persistence and Flyway for database migrations.

Important conventions:

- Each bounded context owns its own schema/tables.
- Entity IDs should use UUIDs in Java.
- UUIDs should be stored as `BINARY(16)` in the database.
- Hibernate should prefer binary UUID storage:

```properties
spring.jpa.properties.hibernate.type.preferred_uuid_jdbc_type=BINARY
```

Example entity ID mapping:

```java
@Id
@GeneratedValue(strategy = GenerationType.UUID)
@Column(name = "id", columnDefinition = "BINARY(16)")
private UUID id;
```

Database setup and generated provisioning files live under `infra/db` and `infra/scripts`.

## Sagas

Sagas coordinate workflows that require multiple bounded contexts. For example, a registration flow may need to create or update person, apprentice, login, education, consent, and audit-log state.

Saga services should:

- Keep orchestration logic out of individual persistence services.
- Call bounded contexts through their internal APIs.
- Use mTLS when communicating with protected services.
- Keep workflow consistency explicit and testable.
- Avoid owning database tables unless the saga has a specific state-tracking requirement.

Keeping sagas in this repository makes the transactional relationships visible together with the database-aware services they coordinate.

## Security

Internal service-to-service communication is protected by mTLS.

Typical rules:

- Database-aware bounded contexts expose `/internal/**`.
- Sagas and handlers call bounded contexts using client certificates.
- Services define an allowlist through `internal.mtls.allowed-callers`.
- Certificates and trust stores are configured through service properties and environment variables.

Common certificate-related environment variables:

```text
TLS_DIR
TLS_KEYSTORE_PASSWORD
TLS_TRUSTSTORE_PASSWORD
CERTIFICATE_FILENAME
SECURITY_ENABLED
MTLS_ENABLED
SERVER_SSL_ENABLED
```

Certificates are provided by the SOP infrastructure team. Local development may require mapping the certificate share or setting `TLS_DIR` to a local certificate folder.

## Build Requirements

- Java 21
- Maven 3.9 or newer, or the included Maven wrapper
- Docker Desktop / Docker Engine for containerized runs
- Access to SOP certificates for full mTLS-enabled integration testing

The project uses:

- Spring Boot 3.5.x
- Maven multi-module builds
- JaCoCo coverage
- Maven Enforcer rules
- GitLab CI for validation, build, and artifact generation

## Build And Test

From the repository root:

```powershell
.\mvnw.cmd clean test
```

On Linux/macOS/CI:

```sh
./mvnw clean test
```

Package all modules:

```powershell
.\mvnw.cmd clean package -DskipTests
```

Build a single service and its required dependencies:

```powershell
.\mvnw.cmd -pl bc-person/person-impl -am clean package
```

## Infrastructure Scripts

Infrastructure scripts live in `infra/scripts`.

Key scripts:

- `gen-compose.sh`: generates `docker-compose.yaml`.
- `gen-db-provision.sh`: generates database provisioning SQL.
- `gen-dockerfiles.sh`: generates service Dockerfiles.
- `gen-manifest.sh`: generates the service/image manifest.
- `gen-ddl.py`: inspects entities and generates Flyway DDL migrations.
- `fix-modifying.py`: checks or corrects Spring Data `@Modifying` usage.
- `check-mariadb-scope.ps1`: validates MariaDB-related scope.
- `clean-artefacts.ps1`: removes generated artifacts.

Generated build/deploy artifacts should be treated as pipeline output unless they are intentionally committed.

## Docker And Deployment

Microservices are deployed as Docker containers. The GitLab pipeline is responsible for:

- Running validation and tests.
- Packaging the Maven modules.
- Generating Dockerfiles.
- Generating database provisioning SQL.
- Generating Docker Compose and manifest artifacts.
- Building and pushing service images.

The pipeline configuration is in `.gitlab-ci.yml`.

The database Docker setup is in `infra/db`. See `infra/db/README.md` for local MariaDB notes.

## Ports

Default ports are configured in each service's `application.properties` with:

```properties
server.port=${SERVER_PORT:<default-port>}
```

The `SERVER_PORT` environment variable can override the default at runtime.

### Database-Aware Services

These services own data and expose internal APIs, usually under `/internal/**`.

| Service | Default port | Notes |
| --- | ---: | --- |
| `bc-apprentice` | `9443` | Database-aware bounded context |
| `bc-audit-log` | `9444` | Database-aware bounded context |
| `bc-consent` | `9445` | Database-aware bounded context |
| `bc-education-line` | `9446` | Database-aware bounded context |
| `bc-institution` | `9447` | Database-aware bounded context |
| `bc-instructor` | `9448` | Database-aware bounded context |
| `bc-message` | `9449` | Database-aware bounded context |
| `bc-notification` | `9450` | Database-aware bounded context |
| `bc-organisation` | `9451` | Database-aware bounded context |
| `bc-person` | `9452` | Database-aware bounded context |
| `bc-sop` | `9453` | Database-aware bounded context |
| `bc-work-hour` | `9454` | Database-aware bounded context |
| `bc-login` | `9455` | Database-aware bounded context |
| `bc-message-person` | `9456` | Database-aware bounded context |
| `bc-education-instructor` | `9457` | Check port ownership before running with `bc-sop-education` |
| `bc-sop-education` | `9457` | Check port ownership before running with `bc-education-instructor` |
| `bc-sop-instructor` | `9458` | Database-aware bounded context |
| `bc-education` | `9459` | Database-aware bounded context |
| `bc-person-notification` | `9460` | Database-aware bounded context |
| `bc-anonymize` | `9499` | Database-aware bounded context |

### Saga Services

Sagas coordinate workflows across multiple bounded contexts.

| Service | Default port | Notes |
| --- | ---: | --- |
| `bc-consent-saga` | `8343` | Transaction/workflow orchestration |
| `bc-login-saga` | `8344` | Transaction/workflow orchestration |
| `bc-registration-saga` | `8345` | Transaction/workflow orchestration |
| `bc-education-line-saga` | `8346` | Transaction/workflow orchestration |
| `bc-education-saga` | `8347` | Transaction/workflow orchestration |
| `bc-anonymize-saga` | `8348` | Transaction/workflow orchestration |
| `bc-message-saga` | `8349` | Transaction/workflow orchestration |

## Development Guidelines

- Keep domain rules in the domain/application layers, not in controllers or persistence adapters.
- Keep persistence details inside the owning bounded context.
- Prefer JPA repositories and structured database migrations over ad-hoc SQL in application code.
- Use Flyway migrations for schema changes.
- Use UUIDs for entity identity and store them as `BINARY(16)`.
- Keep saga orchestration explicit and isolated from entity-level persistence logic.
- Keep endpoint paths lowercase and hyphenated.
- Keep package names lowercase.
- Add or update tests when changing workflow behavior, mappings, security rules, or shared modules.

## Ownership

This project is owned by Ringsted SOP-Data-IT.

New work should keep the repository focused on the data warehouse boundary: persistence services, shared database-facing support, localized information storage, and saga orchestration.
