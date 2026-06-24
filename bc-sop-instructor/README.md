# SOP Insturctor

## Purpose
`bc-sop-instructor` is the bounded context responsible for managing associations between sop references and instructor references. This context handles the lifecycle of relationships between SOP and instructor, including creation, activation, and deactivation of associations.

## Functional scope
The sop-instructor BC will contain support for:
- Creating a new sop-instructor association
- Retrieving associations by SOP reference
- Retrieving associations by instructor reference
- Retrieving a specific association
- Activating an association
- Deactivating an association

## Module layout
The BC is split into two Maven modules:
- `sop-instructor-api`
  - API contracts and shared DTO/domain contract types
- `sop-instructor-impl`
  - Spring Boot implementation (application, domain, adapters)
  - Persistence adapters (JPA and in-memory)
  - Flyway database migrations and i18n resources
  - Runtime configuration

## Architecture style
This BC follow onion and clean architecture principles:
- **Domain**
  - `SopInstructor`: aggregate representing the association between a sop and a instructor
  - Value objects: `SopRef`, `InstructorRef`, `CreatedAt`
  - Composite Value Objects: `SopInstructorId` Contains SopRef and InstructorRef
  - Repository port: `SopInstructorRepositoryPort`
- **Application**
  - `SopInstructorApplicationService`: handles use cases and coordinates domain logic
- **Interface adapters**
  - **Persistence**
    - JPA implementations of the repository
  - **Web**
    - Controller exposing internal endpoints
- **Infrastructure/resources**
  - Configuration, database migration, and i18n resources
  **Entitiy**
  - Composite entity: `SopInstructorEntityId` Annotated with `@Embeddable`, contains composite valeues `SopRef, InstructorRef`
  - Entity: `SopInstructorEntityId` annotated with `@EmbeddedId`, createdAt `LocalDateTime` active `Boolean`

## API endpoints
Base path: `/internal/Sops-Instructors`
- **POST**
    - Create a sop-instructor association
        - Body: `CreateSopInstructorCmd`
        - Returns: `CreatedSopInstructorResult` httpstatus `201 created` with the new DTO of the composite id (SopRef, InstructorRef)
- **GET**
    - Returns: `SopInstructorResponse`
    - `/by-sop/{id}` - Retrieve associations for a sop
      - Returns: `List<SopInstructorResponse>`
    - `/by-instructor/{id}` - Retrieve associations for a instructor
      - Returns: `List<SopInstructorResponse>`
    - `/by-sop/{id}/by-instructor/{id}` - Retrieve a specific association
      - Returns: `SopInstructorResponse` or `httpstatus 204 no content` if not found
- **PUT**
    - `/activate` - Activate association
                Body: `ToggleActivateSopInstructorCmd` 
    - `/deactivate` - Deactivate association
                Body: `ToggleActivateSopInstructorCmd`

## Contracts and data
CreateSopInstructorCmd Fields:
- compositeKey
  - `SopRef`
  - `InstructorRef`
  - Active

CreatedSopInstructorResult Fields:
- CompositeKey
  - `SopRef`
  - `InstructorRef`

SopInstructorResponse Fields:

- CompositeKey
  - `SopRef`
  - `InstructorRef`
- `createdAt`
- `isActive`

ToggleActivateSopInstructorCmd Fields:
- CompositeKey
  - `SopRef`
  - `InstructorRef`
- `isActive`

## Domain model
Aggregate Root:
- `SopInstructor`

Value objects:
- `SopInstructorId`
  - `SopRef` must implement `DomainId`
  - `InstructorRef` must implement `DomainId`
- `CreatedAt` is `LocalDateTime`
- `Active` is `Boolean`

## Invariance
- `SopRef`
  - implements `DomainId`
  - Not Null
- `InstructorRef`
  - implements `DomainId`
  - Not Null
- `CreatedAt`
  - Not Null
  - Format: `dd-MM-yyyy HH:mm:ss`
- `Active`
  - Not Null

**Ports out**
- `RepositoryPort` implements `RepositoryAdapter` injected by `UsecaseService`

**Application Service**
- `UsecaseService` implements `Directory` injects `RepositoryPort`
  - **API**
    - `Directory` injected by `Controller`

**Interfaceweb**
 - `Controller` handles endpoints


## Persistence

- **JPA Entity table**: `SopInstructorEntity` annotated with `@Entity`
- **JPA SpringDataRepository**: implements `JpaRepository`
- **JPA DOMAIN Mapper**
- **Repository Adapter**: implements `RepositoryPort`

## Testing notes
Testing is not yet implemented. Future tests are expected to follow the same structure as other BCs, including domain, application, persistence, and architecture tests.

## Configuration
Main runtime properties are located in:
- `sop-instructor-impl/src/main/resources/application.properties`

Configuration is expected to include standard Spring Boot settings such as service port, profiles, security, and database connectivity.

## Dependencies

### Internal Dependencies
- `sop-instructor-api`
- `common-core`
- `common-data`
- `common-security`
- `common-web`
### External Dependencies
- Spring Boot (web, data-jpa, security, validation)
- Flyway
- MariaDB JDBC driver

## Build and test
From repository root:

- Run tests for this module:
  - `mvn -pl :sop-instructor-impl -am test`
- Run tests with coverage:
  - `mvn -pl :sop-instructor-impl -am clean verify`
- Build this module:
  - `mvn -pl :sop-instructor-impl -am package`
- Run this microservice:
  - `mvn -pl :sop-instructor-impl -am spring-boot:run`