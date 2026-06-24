# Organisation

## Purpose
bc-organisation is the bounded context responsible for managing organisations (with CVR identifiers).

## Current functional scope
Implemented in this BC today:

- Get organisation by ID

## Module layout
This BC contains two Maven modules:

- organisation-api
	- Public application contract (OrganisationDirectory)
	- Request and response DTOs
- organisation-impl
	- Spring Boot runtime module
	- Application service and persistence adapters (JPA)
	- Flyway migration and i18n resources

## Architecture
The implementation follows a simplified, persistence focused architecture without a domain layer:

- Application (application/service/):
	- OrganisationApplicationService: use case orchestration and business logic
- **No domain layer** - Logic operates directly on persistence models
- Interface adapters (interfaceadapters/):
	- Persistence (interfaceadapters/persistence/):
		- JPA: OrganisationSpringDataRepository, OrganisationEntity, OrganisationJpaMapper
- Interface web (interfaceweb/):
	- InternalOrganisationController
- Infrastructure/resources: configuration, migration, and messages

### Why no domain layer?
This BC serves as a **reference data provider** with authoritative organisation data. The scope is intentionally minimal: read only queries against seeded, prepopulated data. There is no business logic because there is no manipulation of data. Data enters the system through migrations or external imports, not through domain operations.

In this context, a domain layer would introduce unnecessary indirection without providing value. The aggregate root, value objects, and repository ports that make sense in BCs with complex business logic would only obscure the simple fact that we're retrieving and returning data.

### Testing implications
Testing in this BC follows its actual role: read only access to seeded organisation data.

- **No domain layer test target** — There are no aggregates, value objects, or domain services to unit test in isolation.
- **No onion architecture expectation here** — Enforcing onion/domain architecture tests would require introducing artificial layers that do not represent real behavior in this BC.

Pragmatic testing strategy for this BC:
- Verify read use cases at the application and controller level
- Verify persistence mapping and repository behavior
- Keep integration tests for end to end read flow (controller -> service -> repository -> database)

This keeps tests accurate to the BC's responsibilities and avoids fake architecture just to satisfy a pattern.

## API endpoints
Base path: /internal/organisations

- GET /{id}
	- Query parameter: id (UUID)
	- Response: OrganisationResponse

## Contracts and data
OrganisationQuery fields:

- id (UUID)

OrganisationResponse fields:

- id (UUID)
- name (String)
- cvr (String)

## Persistence
- JPA table: organisation
- Migration: organisation-impl/src/main/resources/db/migration/V1__init.sql
- JPA adapter uses: OrganisationSpringDataRepository, OrganisationEntity

## Testing notes
The module currently contains:

- Application service tests (with mocked repositories)
- Controller tests
- JPA adapter and mapper tests (where applicable)

Note: Unlike larger BCs, we do not have separate domain layer tests, architecture rule tests, or value object tests because the domain layer does not exist.

## Configuration
Main runtime properties are in organisation-impl/src/main/resources/application.properties.

Current defaults:

- bc.qualifier=organisation
- server.port=9451
- spring.profiles.active=ssl,jpa,shared
- security.enabled=true

## Dependencies
Internal module dependencies:

- organisation-api
- common-core
- common-data
- common-security
- common-web

External technology dependencies:

- Spring Boot (web, data-jpa, security, validation, actuator)
- Flyway
- MariaDB JDBC driver

## Build and test
From repository root:

- Run tests for this module:
	- mvn -pl ./bc-organisation/organisation-impl -am test
- Run tests with coverage:
	- mvn -pl :organisation-impl -am clean verify
- Build this module:
	- mvn -pl ./bc-organisation/organisation-impl -am package
- Run this microservice:
	- mvn -pl :organisation-impl -am spring-boot:run
