# Work hour

## This
This README is a build-up from the global README file in the root of the project

## Purpose
`bc-work-hour` is the bounded context responsible for the lifecycle and ownership of **working hours** in a sop.

This BC contains the domain logic, contracts, and persistence concerns for working hours.

## Functional scope
The working hour BC contains support for:

- Creating a hour hour schedule 
- Retrieving a work hour schedule
- Updating a wotk hour schedule
- Deleting a work hour schedule
- Triggering/creating audit-log entries through integration with `bc-audit_log` **With help from a handler / SAGA**

## Module layout
The BC is split into two Maven modules:

- `work-hour-api`
  - API contracts and shared DTO/domain contract types
- `work-hour-impl`
  - Spring Boot implementation (application/domain/adapters), persistence, migrations, i18n, and runtime configuration

## Architecture style
This BC follows onion and clean architecture principles:

- **Domain** is central and does not depend on infrastructure
- **Application** orchestrates use-case flows
- **Interface adapters** handle inbound/outbound integration (web, persistence, messaging)
- **Infrastructure/resources** contain technical concerns such as DB migration and i18n

## Integration and technical boundaries

- **Audit logging**: business-relevant actions can be logged through `bc-audit_log` **With a helper / SAGA**
- **Database**: Flyway migration is located in `work-hour-impl/src/main/resources/db/migration`
- **Localization**: message bundles are in `work-hour-impl/src/main/resources/i18n`
- **Runtime**: service is configured as a Spring Boot app (`work-hour-impl`) with port `9454` as default

## Domain Model

### Core Entities
**(Description here)**
Such as work-hour entity

### Value Objects
**(Description here)**

## Use Cases

### Create an education line under existing Education
**Requirements**
- Create a work hour schedule in table sopinfo_work-hour
- Check if given work hour schedule exist
- Return created work hour schedule if created
- Return corresponding error if something happend.

### Retrieve work hour schedule
**Requirements**
- **WILL THE PAYLOAD BE AN EDUCATION LINE ID OR NAME?**
- Check if work hour schedule exist
- Return the work hour schedule if it exist
- Return corresponding error if something happend


### Update working hours
**Requirements**
- Check if work hour schedule exist
- If it exist update it's start-time and/or end-time
- Return newly updated work hour schedule
- Return corresponding error if something happend

### Can create a audit-log using bc-audit_log (Won't happen in this bc, but is a use case)

## API Endpoints
- **GET**
  - /test

- **POST**
  - /test

- **PUT**
  - /test

- **DELETE**
  - /test

## Dependencies

### Internal Dependencies
- application.properties
- pom.xml

### External Dependencies
- Docker
- Database: Mariadb

## Configuration

### Envoriment Variables
Handled by the pipeline

### Dockerfile
Dockerfile makes the microservice into a .jar file that can be used by our handlers.

## Current status
This BC currently contains scaffolded package structure, baseline configuration, and placeholder migration/messages.

The README describes the intended bounded-context responsibilities and module content, while implementation details will evolve as the domain is finalized.