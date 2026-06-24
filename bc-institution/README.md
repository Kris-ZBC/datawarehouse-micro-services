# Institution

## Purpose
`bc-institution` is the bounded context responsible for the lifecycle and ownership of Institution records (Skoler/Uddannelsessteder) within the SOP system.

## Functional scope
The Institution BC contains support for:

- Retrieving Institution data by ID

## Module layout
The BC is split into two Maven modules:

- `institution-api`
    - API contracts and shared DTO/application contract types (InstitutionQuery, InstitutionResponse)
- `institution-impl`
    - Spring Boot implementation (application/adapters), persistence, migrations, i18n, and runtime configuration

## Architecture style
This BC follows a pragmatic layered architecture focused on clear separation of concerns without unnecessary boilerplate:

- **Application service**: orchestrates the use-case flows, logging, and business exception handling
- **Interface adapters**: handle inbound/outbound integration (Web Controllers, JPA Persistence)
- **Infrastructure/resources**: handles technical cross-cutting concerns like Flyway migrations and resource bundles

## Integration and technical boundaries

- **Database**: Flyway migration is located in institution-impl/src/main/resources/db/migration
- **Localization**: Message bundles are in institution-impl/src/main/resources/i18n
- **Runtime**: Service is configured as a Spring Boot app (institution-impl) with port 9447 as default

## Use Cases

### Retrieve Institution
- Success: Fetches the record by its UUID and maps it to an InstitutionResponse
- Error (Not Found): If the ID doesn't exist, logs a WARN and throws a NotFoundException (404) with the message key institution.notFound

## API Endpoints
- **GET**
    - Get Institution record by ID
    - /internal/institution/{id}

## Dependencies

### Internal Dependencies
- application.properties
- pom.xml

### External Dependencies
- Docker
- Database: MariaDB

## Configuration

### Environment Variables
Handled by the pipeline.

## Architectural Decisions

### 1. Where is the Domain Layer?
The Institution module implements a **Pragmatic Layered Architecture**. Since current requirements focus on straightforward data retrieval without complex invariant logic, a dedicated `domain` package was omitted to avoid "Boilerplate Overkill." 

Business rules (such as 404 handling and logging) are orchestrated within the `InstitutionApplicationService`.

### 2. Absence of Architecture Tests
ArchUnit or Onion Architecture tests have been intentionally excluded. Because the module is "thin," these tests would primarily enforce a complexity that does not exist, leading to brittle tests that hinder development.

## Current status
The Institution BC is fully functional for retrieval operations.
- **Application layer**: Service-level logging and exception mapping are active.
- **Persistence**: JPA mapping to the institution table is established.
- **Testing**: Coverage includes Unit tests for the Service and MockMvc tests for the Web layer, focusing on contract validation and error states.