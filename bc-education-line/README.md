# Education Line

## Purpose
bc-education-line is the bounded context responsible for managing education lines under an education reference.

## Current functional scope
Implemented in this BC today:

- Create an education line
- Get all education lines
- Get education line by id
- Update education line name
- Update education line duration
- Deactivate education line
- Activate education line

## Module layout
This BC contains two Maven modules:

- education-line-api
	- Public application contract (EducationLineDirectory)
	- Request and response DTOs
- education-line-impl
	- Spring Boot runtime module
	- Application service and domain logic
	- Persistence adapters (JPA and in-memory)
	- Flyway migration and i18n resources

## Architecture
The implementation follows clean/onion architecture:

- Domain (domain/):
	- Service: EducationLineDomain, EducationLineDomainService
	- Model: EducationLine aggregate
	- Value objects: EducationLineId, EducationLineName, EducationLineDuration, EducationLineCreatedAt, EducationRef
	- Ports out: EducationLineRepositoryPort
- Application (application/service/):
	- EducationLineApplicationService: use-case orchestration
- Interface adapters (interfaceadapters/):
	- Persistence (interfaceadapters/persistence/):
		- JPA: EducationLineRepositoryAdapter, EducationLineSpringDataRepository, EducationLineEntity, EducationLineJpaMapper
		- In-memory: InMemoryEducationLineRepositoryImpl
- Interface web (interfaceweb/):
	- EducationLineController
- Infrastructure/resources: configuration, migration, and messages

## API endpoints
Base path: /internal/educationlines

- POST /create
	- Body: CreateEducationLineCmd
- GET /getAll
- GET /getById?id={uuid}
- PUT /updateName?id={uuid}
	- Body: UpdateEducationLineNameCmd
- PUT /updateDuration?id={uuid}
	- Body: UpdateEducationLineDurationCmd
- PUT /deactivate?id={uuid}
- PUT /activate?id={uuid}

## Contracts and data
CreateEducationLineCmd fields:

- name
- durationYears
- durationMonths
- durationDays
- educationRef

EducationLineResponse fields:

- id
- name
- durationYears
- durationMonths
- durationDays
- educationRef
- createdAt
- isActive

## Domain model
Core aggregate:

- EducationLine

Value objects:

- EducationLineId
- EducationLineName
- EducationLineDuration
- EducationLineCreatedAt
- EducationRef

## Persistence
- JPA table: education_line
- Migration: education-line-impl/src/main/resources/db/migration/V1__init.sql
- JPA adapter bean qualifier: JpaEducationLineRepository
- In-memory adapter bean qualifier: inMemoryEducationLineRepository

Note: the application service currently injects the JPA adapter by qualifier.

## Testing notes
The module currently contains:

- Domain and value object tests
- Architecture rule tests
- Application service tests
- Controller tests
- JPA adapter and mapper tests (Mockito-based)
- In-memory repository tests

The in-memory repository acts as an in-process fake repository (stateful list-backed implementation), not as a real JPA database.

## Configuration
Main runtime properties are in education-line-impl/src/main/resources/application.properties.

Current defaults include:

- bc.qualifier=education_line
- server.port=9446
- spring.profiles.active=ssl,jpa,shared
- security.enabled=true

## Dependencies
Internal module dependencies:

- education-line-api
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
	- mvn -pl ./bc-education-line/education-line-impl -am test
- Run tests with coverage:
	- mvn -pl :education-line-impl -am clean verify
- Build this module:
	- mvn -pl ./bc-education-line/education-line-impl -am package
- Run this microservice:
	- mvn -pl :education-line-impl -am spring-boot:run