# Education

## Purpose
`bc-education` is the bounded context responsible for the lifecycle and ownership of **educational programs** within the SOP system.

This BC contains the domain logic, contracts, and persistence concerns for educations, including categorisation and activation state management.

## Functional scope
The Education BC contains support for:

- Creating an education
- Retrieving education data by id
- Retrieving all educations
- Updating an education (name and/or category)
- Activating an education
- Deactivating an education

## Module layout
The BC is split into two Maven modules:

- `education-api`
    - API contracts and shared DTO/domain contract types
- `education-impl`
    - Spring Boot implementation (application/domain/adapters), persistence, migrations, i18n, and runtime configuration

## Architecture style
This BC follows onion and clean architecture principles:

- **Domain** is central and does not depend on infrastructure
- **Application** orchestrates use-case flows
- **Interface adapters** handle inbound/outbound integration (web, persistence, messaging)
- **Infrastructure/resources** contain technical concerns such as DB migration and i18n

## Integration and technical boundaries

- **Database**: Flyway migration is located in education-impl/src/main/resources/db/migration
- **Localization**: message bundles are in education-impl/src/main/resources/i18n
- **Runtime**: service is configured as a Spring Boot app (education-impl) with port 9459 as default

## Domain Model

### Core Entities
**Education** is the aggregate root of this BC. It represents a named, categorised educational program and carries an activation state.

### Value Objects

- **EducationId** — wraps UUID, uniquely identifies an Education
- **EducationName** — wraps String, the human-readable name
- **EducationCategory** — wraps String, classifies the education

## Use Cases

### Create an education
- Success: Creates a new record in sopinfo_education (default active = false)
- Error (Duplicate Name): If the name exists, logs a WARN and throws a ValidationException(400)
- Output: Returns the newly created EducationResponse

### Retrieve education
- Success: Fetches the record by its UUID
- Error (Not Found): If the ID doesn't exist, logs a WARN and throws a NotFoundException(404)

### Retrieve all educations
- Success: Returns a list of all educations
- Note: Returns an empty list if no records exist (this is not considered an error)

### Update education
- Success: Re-builds and persists the aggregate with a new name/category
- Error (Not Found): If the ID doesn't exist, logs a WARN and throws a NotFoundException (404)
- Error (Conflict): If the new name is already taken by a different education, throws a ValidationException(400)

### Activate education
- Success: Toggles the active state and saves it
- Error (Not Found): Logs a WARN and throws a NotFoundException(404) if the ID is missing

### Deactivate education
- Success: Toggles the active state and saves it
- Error (Not Found): Logs a WARN and throws a NotFoundException(404) if the ID is missing

## API Endpoints
- **POST**
    - Create a new education
    - /internal/education/create

- **GET**
    - Get education by ID
    - /internal/education/{id}

- **GET**
    - List all educations
    - /internal/education/all

- **PUT**
    - Update name and category
    - /internal/education/{id}

- **PUT**
    - Set active status to true
    - /internal/education/{id}/activate

- **PUT**
    - Set active status to false
    - /internal/education/{id}/deactivate

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

### Dockerfile
Dockerfile makes the microservice into a .jar file that can be used by our handlers.

## Current status
The Education BC is fully functional at the application and domain layers.
- **Domain logic**: Unique name constraints and state management (activate/deactivate) are implemented.
- **API**: REST Controller is mapped to the internal endpoints.
- **Testing**: Unit tests cover succes paths, 404 Not Found scenarios, and 400 Conflict/validation cases.