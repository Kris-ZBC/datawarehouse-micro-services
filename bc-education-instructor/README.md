# Education Instructor

## Purpose
bc-education-instructor is the bounded context responsible for managing associations between education references and instructor references.

## Current functional scope
Implemented in this BC today:

- Create an education-instructor association
- Get all education-instructor associations
- Get associations by educationRef
- Get associations by instructorRef
- Get association by educationRef and instructorRef
- Deactivate association
- Activate association

## Module layout
This BC contains two Maven modules:

- education-instructor-api
	- Public application contract (EducationInstructorDirectory)
	- Request and response DTOs
- education-instructor-impl
	- Spring Boot runtime module
	- Application service and domain logic
	- Persistence adapters (JPA and in-memory)
	- Flyway migration and i18n resources

## Architecture
The implementation follows clean/onion architecture:

- Domain (domain/):
	- Service: EducationInstructorDomain, EducationInstructorDomainService
	- Model: EducationInstructor aggregate
	- Value objects: EducationInstructorId, EducationRef, InstructorRef, CreatedAt
	- Ports out: EducationInstructorRepositoryPort
- Application (application/service/):
	- EducationInstructorApplicationService: use-case orchestration
- Interface adapters (interfaceadapters/):
	- Persistence (interfaceadapters/persistence/):
		- JPA: EducationInstructorRepositoryAdapter, EducationInstructorSpringDataRepository, EducationInstructorEntity, EducationInstructorJpaMapper
		- In-memory: InMemoryEducationInstructorRepositoryImpl
- Interface web (interfaceweb/):
	- EducationInstructorController
- Infrastructure/resources: configuration, migration, and messages

## API endpoints
Base path: /internal/educationinstructors

- POST /create
	- Body: CreateEducationInstructorCmd
- GET /getAll
- GET /getByEducationRef?educationRef={uuid}
- GET /getByInstructorRef?instructorRef={uuid}
- GET /getByEducationRefAndInstructorRef?educationRef={uuid}&instructorRef={uuid}
- PUT /deactivate?educationRef={uuid}&instructorRef={uuid}
- PUT /activate?educationRef={uuid}&instructorRef={uuid}

## Contracts and data
CreateEducationInstructorCmd fields:

- educationRef
- instructorRef

EducationInstructorResponse fields:

- id
- educationRef
- instructorRef
- createdAt
- isActive

## Domain model
Core aggregate:

- EducationInstructor

Value objects:

- EducationInstructorId
- EducationRef
- InstructorRef
- CreatedAt

## Persistence
- JPA table: education_instructor
- Migration: education-instructor-impl/src/main/resources/db/migration/V1__init.sql
- JPA adapter bean qualifier: JpaEducationInstructorRepository
- In-memory adapter bean qualifier: inMemoryEducationInstructorRepository

Note: the application service currently injects the JPA adapter by qualifier.

## Testing notes
The module currently contains:

- Architecture rule tests

Current architecture tests:

- ApplicationArchitectureTest
- DomainArchitectureTest
- OnionArchitectureTest

The in-memory repository acts as an in-process fake repository (stateful list-backed implementation), not as a real JPA database.

## Configuration
Main runtime properties are in education-instructor-impl/src/main/resources/application.properties.

Current defaults include:

- bc.qualifier=education_instructor
- server.port=9457
- spring.profiles.active=ssl,jpa,shared
- security.enabled=true

## Dependencies
Internal module dependencies:

- education-instructor-api
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
	- mvn -pl ./bc-education-instructor/education-instructor-impl -am test
- Run tests with coverage:
	- mvn -pl :education-instructor-impl -am clean verify
- Build this module:
	- mvn -pl ./bc-education-instructor/education-instructor-impl -am package
- Run this microservice:
	- mvn -pl :education-instructor-impl -am spring-boot:run
