# SOP

## Purpose
`bc-sop` is the bounded context responsible for the lifecycle and ownership of School-based Apprenticeship (Skoleoplæring) records within the SOP system.

## Functional scope
The SOP BC contains support for:

- Retrieving SOP data by ID

## Module layout
The BC is split into two Maven modules:

- `sop-api`
    - API contracts and shared DTO/application contract types (SOPQuery, SOPResponse)
- `sop-impl`
    - Spring Boot implementation (application/adapters), persistence, migrations, i18n, and runtime configuration

## Architecture style
This BC follows a pragmatic layered architecture focused on clear separation of concerns without unnecessary boilerplate:

- **Application service** orchestrates the use-case flows, logging, and business exception handling
- **Interface adapters** handle inbound/outbound integration (Web Controllers, JPA Persistence)
- **Infrastructure/resources** handles technical cross-cutting concerns like Flyway migrations and resource bundles

## Integration and technical boundaries

- **Database**: Flyway migration is located in sop-impl/src/main/resources/db/migration
- **Localization**: message bundles are in sop-impl/src/main/resources/i18n
- **Runtime**: service is configured as a Spring Boot app (sop-impl) with port 9453 as default

## Use Cases

### Retrieve SOP
- Success: fetches the record by its UUID and maps it to a SOPResponse
- Error (Not Found): if the ID doesn't exist, logs a WARN and throws a NotFoundException (404) with the message key sop.notFound

## API Endpoints
- **GET**
    - Get SOP record by ID
    - /internal/sop/{id}

## Dependencies

### Internal Dependencies
- application.properties
- pom.xml

### External Dependencies
- Docker
- Database: Mariadb

## Configuration

### Environment Variables
Handled by the pipeline

## Architectural Decisions

### 1. Where is the Domain Layer?
The SOP module currently implements a **Pragmatic Layered Architecture**. Since the current business requirements focus on straightforward data retrieval and storage without complex invariant logic or state machines, a dedicated `domain` package was omitted to avoid "Boilerplate Overkill". 

Business rules (such as 404 handling and logging) are orchestrated within the `SOPApplicationService`.

### 2. Absence of Architecture Tests
ArchUnit or Onion Architecture tests have been intentionally excluded. Because the module is "thin," these tests would primarily enforce a complexity that does not exist, leading to brittle tests that hinder rather than help development.

## Current status
The SOP BC is fully functional for retrieval operations.
- **Application layer**: service-level logging and exception mapping are active.
- **Persistence**: JPA mapping to the sop.sop table is established.
- **Testing**: coverage includes Unit tests for the Service and MockMvc tests for the Web layer, focusing on contract validation and error states.