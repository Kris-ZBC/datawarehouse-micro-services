# Message Person

## Purpose
`bc-message-person` is the bounded context responsible for managing associations between message references and person references. This context handles the lifecycle of relationships between messages and persons, including creation, activation, and deactivation of associations.

## Functional scope
The message-person BC will contain support for:
- Creating a new message-person association
- Retrieving associations by message reference
- Retrieving associations by person reference
- Retrieving a specific association
- Activating an association
- Deactivating an association

## Module layout
The BC is split into two Maven modules:
- `message-person-api`
  - API contracts and shared DTO/domain contract types
- `message-person-impl`
  - Spring Boot implementation (application, domain, adapters)
  - Persistence adapters (JPA and in-memory)
  - Flyway database migrations and i18n resources
  - Runtime configuration

## Architecture style
This BC follow onion and clean architecture principles:
- **Domain**
  - `MessagePerson`: aggregate representing the association between a message and a person
  - Value objects: `MessageRef`, `PersonRef`, `CreatedAt`
  - Composite Value Objects: `MessagePersonId` Contains MessageRef and PersonRef
  - Repository port: `MessagePersonRepositoryPort`
- **Application**
  - `MessagePersonApplicationService`: handles use cases and coordinates domain logic
- **Interface adapters**
  - **Persistence**
    - JPA implementations of the repository
  - **Web**
    - Controller exposing internal endpoints
- **Infrastructure/resources**
  - Configuration, database migration, and i18n resources
  **Entitiy**
  - Composite entity: `MessagePersonEntityId` Annotated with `@Embeddable`, contains composite valeues `messageRef, personRef`
  - Entity: `MessagePersonEntityId` annotated with `@EmbeddedId`, createdAt `LocalDateTime` active `Boolean`

## API endpoints
Base path: `/internal/messages-persons`
- **POST**
    - Create a message-person association
        - Body: `CreateMessagePersonCmd`
        - Returns: `CreatedMessagePersonResult` httpstatus `201 created` with the new DTO of the composite id (messageRef, personRef)
- **GET**
    - Returns: `MessagePersonResponse`
    - `/by-message/{id}` - Retrieve associations for a message
      - Returns: `List<MessagePersonResponse>`
    - `/by-person/{id}` - Retrieve associations for a person
      - Returns: `List<MessagePersonResponse>`
    - `/by-message/{id}/by-person/{id}` - Retrieve a specific association
      - Returns: `MessagePersonResponse` or `httpstatus 204 no content` if not found
- **PUT**
    - `/activate` - Activate association
                Body: `ToggleActivateMessagePersonCmd` 
    - `/deactivate` - Deactivate association
                Body: `ToggleActivateMessagePersonCmd`

## Contracts and data
CreateMessagePersonCmd Fields:
- compositeKey
  - `messageRef`
  - `personRef`
  - Active

CreatedMessagePersonResult Fields:
- CompositeKey
  - `messageRef`
  - `personRef`

MessagePersonResponse Fields:

- CompositeKey
  - `messageRef`
  - `personRef`
- `createdAt`
- `isActive`

ToggleActivateMessagePersonCmd Fields:
- CompositeKey
  - `messageRef`
  - `personRef`
- `isActive`

## Domain model
Aggregate Root:
- `MessagePerson`

Value objects:
- `MessagePersonId`
  - `MessageRef` must implement `DomainId`
  - `PersonRef` must implement `DomainId`
- `CreatedAt` is `LocalDateTime`
- `Active` is `Boolean`

## Invariance
- `MessageRef`
  - implements `DomainId`
  - Not Null
- `PersonRef`
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

- **JPA Entity table**: `MessagePersonEntity` annotated with `@Entity`
- **JPA SpringDataRepository**: implements `JpaRepository`
- **JPA DOMAIN Mapper**
- **Repository Adapter**: implements `RepositoryPort`

## Testing notes
Testing is not yet implemented. Future tests are expected to follow the same structure as other BCs, including domain, application, persistence, and architecture tests.

## Configuration
Main runtime properties are located in:
- `message-person-impl/src/main/resources/application.properties`

Configuration is expected to include standard Spring Boot settings such as service port, profiles, security, and database connectivity.

## Dependencies

### Internal Dependencies
- `message-person-api`
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
  - `mvn -pl :message-person-impl -am test`
- Run tests with coverage:
  - `mvn -pl :message-person-impl -am clean verify`
- Build this module:
  - `mvn -pl :message-person-impl -am package`
- Run this microservice:
  - `mvn -pl :message-person-impl -am spring-boot:run`