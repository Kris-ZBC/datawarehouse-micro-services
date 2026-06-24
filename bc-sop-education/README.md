# Message Person

## Purpose
`bc-sop-education` is the bounded context responsible for managing associations between sop references and education-line references. This context handles the lifecycle of relationships between sops and education-lines, including creation, activation, and deactivation of associations.

## Functional scope
The sop-education-line BC will contain support for:
- Creating a new sop-education-line association
- Retrieving associations by sop reference
- Retrieving associations by education-line reference
- Retrieving a specific association
- Activating an association
- Deactivating an association

## Module layout
The BC is split into two Maven modules:
- `sop-education-api`
  - API contracts and shared DTO/domain contract types
- `sop-education-impl`
  - Spring Boot implementation (application, domain, adapters)
  - Persistence adapters (JPA and in-memory)
  - Flyway database migrations and i18n resources
  - Runtime configuration

## Architecture style
This BC follow onion and clean architecture principles:
- **Domain**
  - `SopEducation`: aggregate representing the association between a message and a person
  - Value objects: `SopRef`, `EducationRef`, `CreatedAt`
  - Composite Value objects: `SopEducationId` COntains SopRef and EducationLineRef
  - Repository port: `SopEducationRepositoryPort`
- **Application**
  - `SopEducationApplicationService`: handles use cases and coordinates domain logic
- **Interface adapters**
  - **Persistence**
    - JPA implementations of the repository
  - **Web**
    - Controller exposing internal endpoints
- **Infrastructure/resources**
  - Configuration, database migration, and i18n resources
- **Entity**
  - Composite entity: `SopEducationEntityId` Annotated with `@Embeddable`, Contains composite values: `sopRef, educationRef`
  - Entity: `SopEducationEntityId` annotated with `@EmbeddedId`, createdAt `LocalDateTime` active `Boolean`   

## API endpoints
Base path: `/internal/sop-education`
- **POST**
     - Create a sop-education-line association
        - Body: `CreateSopEducationCmd`
        - Returns: `CreatedSopEducationResult` htttpstatus: `201 created` with the new DTO of the composite id (sopRef, educationRef)
- **GET**
    - Returns: `SopEducationResponse`  
    - `/by-sop/{id}` - Retrieve associations for a sop with id 
      - Returns: `List<SopEducationResponse>`
    - `/by-education-line/{id}` - Retrieve associations for an education-line id
      - Returns: `List<SopEducationResponse>`
    - `/by-sop/{id}/by-education/{id}` - Retrieve a specific association
      Returns: `SopEducationResponse` or `httpstatus 204 no content`if not found  
- **PUT**
    - `/activate` - Activate association
                  Body: `ToggleActivateSopEducationCmd`     
    - `/deactivate` - Deactivate association
                    Body: `ToggleActivateSopEducationCmd`

## Contracts and data
CreateSopEducationLineCmd Fields:
  - CompositeKey
    - `sopRef`
    - `educationRef`
  - Active

CreatedSopEducationLineResult Fields:
  - CompositeKey
    - `sopRef`
    - `educationLineRef`

SopEducationLineResponse Fields:
  - CompositeKey
    - `sopRef`
    - `educationRef`
- `createdAt`
- `active`

ToggleActivateSopEducationLineCmd Fields:
  - CompositeKey
    - `sopRef`
    - `educationRef`
- `active`


## Domain model
Aggregate Root:
- `SopEducation`

Value objects:
- `SopEducationLineId`
  - `SopRef` must implement `DomainId`
  - `EducationRef` must implement `DomainId`
- `CreatedAt` is `LocalDateTime`
- `Active` is `Boolean`

## Invariants
- `SopRef` 
  - implements `DomainId`
  - Not Null
- `EducationRef`
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
  - **Api**
    - `Directory` injected by `Controller`

**Interfaceweb**
  - `Controller` handles endpoints

## Persistence

- **JPA ENtity table**: 
  - `SopEducationEntity` annotated with `@Entity`
**JPA SpingDataRepository** implements `JpaRepository`
**JPA DOMAIN Mapper**  
**Repository Adaper** implements `RepositoryPort`  

## Testing notes
Testing is not yet implemented. Future tests are expected to follow the same structure as other BCs, including domain, application, persistence, and architecture tests.

## Configuration
Main runtime properties are located in:
- `sop-education-impl/src/main/resources/application.properties`

Configuration is expected to include standard Spring Boot settings such as service port, profiles, security, and database connectivity.

## Dependencies

### Internal Dependencies
- `sop-education-api`
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
  - `mvn -pl :sop-education-impl -am test`
- Run tests with coverage:
  - `mvn -pl :sop-education-impl -am clean verify`
- Build this module:
  - `mvn -pl :sop-education-impl -am package`
- Run this microservice:
  - `mvn -pl :sop-education-impl -am spring-boot:run`