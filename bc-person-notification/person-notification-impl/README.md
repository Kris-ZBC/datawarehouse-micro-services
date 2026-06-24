# Person notification

## This
This README is a build-up from the global README file in the root of the project

## Purpose
`bc-person-notification` is the bounded context responsible for the lifecycle and ownership of **person-notification relations** in SOP.

This BC contains the domain logic, contracts, and persistence concerns for relations between a person and a notification.

## Functional scope
The person-notification BC contains support for:

- Creating a person-notification relation
- Retrieving all person-notification relations
- Retrieving person-notification relations by person reference
- Retrieving person-notification relations by notification reference
- Preventing duplicate relations between the same `personRef` and `notificationRef`

## Module layout
The BC is split into two Maven modules:

- `person-notification-api`
  - API contracts and shared DTO/domain contract types
- `person-notification-impl`
  - Spring Boot implementation (application/domain/adapters), persistence, migrations, i18n, and runtime configuration

## Architecture style
This BC follows onion and clean architecture principles:

- **Domain** is central and does not depend on infrastructure
- **Application** orchestrates use-case flows
- **Interface adapters** handle inbound/outbound integration (web, persistence, messaging)
- **Infrastructure/resources** contain technical concerns such as DB migration and i18n

## Integration and technical boundaries

- **Database**: Flyway migration is located in `person-notification-impl/src/main/resources/db/migration`
- **Localization**: message bundles are in `person-notification-impl/src/main/resources/i18n`
- **Runtime**: service is configured as a Spring Boot app (`person-notification-impl`) with port `9460` as default

## Domain Model

### Core Entities
**PersonNotification**

Represents a relation between:

- a `Person`
- a `Notification`

The entity stores:

- `id`
- `personRef`
- `notificationRef`

This BC does not own the actual `Person` or `Notification` aggregates.
It only owns the relation between them.

### Value Objects
The BC contains value objects such as:

- `PersonNotificationId`
- `PersonRef`
- `NotificationRef`

These value objects are used to keep the domain model explicit and type-safe.

## Use Cases

### Create person-notification relation
**Requirements**
- Create a relation in table `person_notification`
- Check if the relation already exists
- Prevent duplicate `personRef` and `notificationRef` combinations
- Return the created relation if created
- Return corresponding error if something happened

### Retrieve all person-notification relations
**Requirements**
- Fetch all existing person-notification relations
- Return a list of relations
- Return corresponding error if something happened

### Retrieve relations by person reference
**Requirements**
- Check if `personRef` is provided
- Fetch all relations for the given person
- Return the matching relations
- Return corresponding error if something happened

### Retrieve relations by notification reference
**Requirements**
- Check if `notificationRef` is provided
- Fetch all relations for the given notification
- Return the matching relations
- Return corresponding error if something happened

## API Endpoints
- **GET**
  - `/internal/person-notifications`
  - `/internal/person-notifications?personRef={uuid}`
  - `/internal/person-notifications?notificationRef={uuid}`

- **POST**
  - `/internal/person-notifications`

## Dependencies

### Internal Dependencies
- `application.properties`
- `pom.xml`

### External Dependencies
- Docker
- Database: MariaDB

## Configuration

### Environment Variables
Handled by the pipeline

### Dockerfile
Dockerfile makes the microservice into a `.jar` file that can be used by our handlers.

## Current status
This BC currently contains scaffolded package structure, baseline configuration, persistence setup, migration setup, and i18n files.

The BC is focused on handling the relation between person and notification references, while additional use cases and tests can be expanded as the domain is finalized.