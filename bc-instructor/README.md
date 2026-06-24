# Instructor

## This
This README is a build-up from the global README file in the root of the project.

---

## Purpose
`bc-instructor` is the bounded context responsible for the lifecycle and ownership of **Instructor** entities in the system.

This BC contains the domain logic, contracts, and persistence concerns for instructors.

The Instructor is treated as a domain concept that references a **Person** but does not own or manage Person data.

---

## Functional scope
The Instructor BC supports:

- Creating an instructor
- Retrieving all instructors
- Retrieving an instructor by ID

The BC maintains instructor identity and references external Person entities via `personRef`.

---

## Module layout
The BC is split into two Maven modules:

- `instructor-api`
  - API contracts and DTOs
- `instructor-impl`
  - Spring Boot implementation (domain, application, adapters, persistence, configuration)

---

## Architecture style
This BC follows Onion and Clean Architecture principles:

- **Domain** is central and independent of frameworks
- **Application** orchestrates use cases
- **Interface adapters** handle REST, persistence, and external integrations
- **Infrastructure/resources** handle technical concerns (DB, config, etc.)

---

## Integration and technical boundaries

- **Person BC**:
  - Instructor references a Person via `personRef`
  - No duplication or ownership of Person data

- **Database**:
  - Flyway migrations located in:
    - `instructor-impl/src/main/resources/db/migration`

- **Localization**:
  - Message bundles located in:
    - `instructor-impl/src/main/resources/i18n`

- **Runtime**:
  - Runs as a Spring Boot service (`instructor-impl`)
  - Default port: 9948

---

## Domain Model

### Core Entities
- **Instructor**
  - Aggregate root
  - Represents an instructor in the system
  - Contains identity and reference to a Person (`personRef`)

### Value Objects
- **InstructorId**
  - Unique identifier (UUID)

- **PersonRef**
  - Reference to Person BC
  - Ensures decoupling between bounded contexts

---

## Use Cases

### Create Instructor
**Requirements**
- Create a new instructor with a valid `personRef`
- Ensure no duplicate instructor exists (if applicable)
- Persist instructor in database
- Return created instructor
- Return error if validation fails

---

### Retrieve Instructor by ID
**Requirements**
- Validate instructor ID
- Check if instructor exists
- Return instructor if found
- Return error if not found

---

### Retrieve All Instructors
**Requirements**
- Fetch all instructors from database
- Return list of instructors

---

## API Endpoints

- **GET**
  - `/internal/instructors`
  - `/internal/instructors/{id}`

- **POST**
  - `/internal/instructors`

---

## Dependencies

### Internal Dependencies
- `application.properties`
- Maven (`pom.xml`)

### External Dependencies
- Spring Boot
- Docker
- Database: MariaDB

---

## Configuration

### Environment Variables
Handled by the deployment pipeline.

---

### Dockerfile
Builds the service into a `.jar` file for containerized deployment.

---

## Current status
This BC currently contains:

- Core domain structure
- Basic CRUD functionality for Instructor
- Integration boundaries toward Person BC

The implementation will evolve as domain rules and invariants are refined.