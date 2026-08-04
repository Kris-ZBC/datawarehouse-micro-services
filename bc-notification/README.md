# BC-Notification

## Purpose
`bc-notification` is the bounded context responsible for creating and managing notifications. A notification represents a piece of information that can be delivered to a person through integrations with `bc-person-notification`

## Functional scope
The notification BC contains support for:
- Create and delete notifications
- Make notifications seen
- Retrieve all notifications by person reference
- Creating audit-log entries through integration with `bc-audit_log`  **With a helper / SAGA**


## Module layout
The BC is split into two Maven modules:
- `notification-api`
	- API contracts and shared DTO/domain contract types
- `notification-impl`
	- Spring Boot implementation (application/domain/adapters), persistence, migrations, i18n, and runtime configuration

## Architecture style
This BC follow onion and clean architecture principles:
- **Domain** is central and does not depend on infrastructure
- **Application** orchestrates use-case flows
- **Interface adapters** handle inbound/outbound integration (web, persistence, messaging)
- **Infrastructure/resources** contain technical concerns such as DB migration and i18n

## Integration and technical boundaries

- **Audit logging**: business-relevant actions can be logged through `bc-audit_log` **With a helper / SAGA**
- **Database**: Flyway migration is located in `notification-impl/src/main/resources/db/migration`
- **Localization**: message bundles are in `notification-impl/src/main/resources/i18n`
- **Runtime**: service is configured as a Spring Boot app (`notification-impl`) with port `9450` as default

## DTOs

- `CreateNotificationCmd`
	- **messageRef** UUID

- `CreateNotificationResult`
	- **id** UUID

- `NotificationResponse`
	- **id** UUID
	- **seen** boolean
	- **createdAt** LocalDateTime
	- **messageRef** UUID


## Domain Model

### Core Entities
- **Notification**
	- represents a notification sent to a person when they receive a message

### Value Objects
- `NotificationId`
- `MessageRef`
- `CreatedAtTimestamp`

## Use Cases

| UC-0001 | Create a notification |
|---------|----------------|
| **Description** | Creates a new notification |
| **Actor** | Instructor |
| **Trigger** | a request is sent to create a notification |
| **Preconditions** | **•** Actor is authenticated |
| **Postconditions** | **•** Notification is saved to the database with `seen = false`<br>**•** Creation is logged |
| **Flow** | 1. Receive request to create notification<br>2. validate its not null<br>3. notification is created<br>4. save it via repoitory port<br>5. Log through `bc-audit-log`<br>6. Return `NotificationResponse` |
| **Alternative Flows** | 2a. Command is null → `ValidationException`<br>4a. Persistence error → `PersistenceError`<br>5a. Audit log fails → `LogError` |
| **Priority** | High |

<br>

| UC-0002 | delete a notification |
|---------|----------------|
| **Description** | Delete a notification |
| **Actor** | Instructor |
| **Trigger** | Receive request to delete a notification |
| **Preconditions** | **•** Actor is authenticated<br>**•** Notification exists |
| **Postconditions** | **•** Deletion is logged<br> **•** Notification is deleted from the database |
| **Flow** | 1. Receive request to delete notification<br>2. Validate ID is not null<br>3. Find notification by id<br>4. Delete notification<br>5. Log through `bc-audit-log`<br>6. Return confirmation |
| **Alternative Flows** | 2a. ID is null → `ValidationException`<br>3a. Notification is not found → `NotFoundException`<br>4a. Persistance error → `PersistanceError`<br>5a. Audit log fails  → `LogError` |
| **Priority** | High |

<br>

| UC-0003 | Make notification seen |
|---------|----------------|
| **Description** | Marks notification as seen by the recipient |
| **Actor** | Person |
| **Trigger** | Receive request to mark notification as seen |
| **Preconditions** | **•** Actor is authenticated<br>**•** Notification exists<br>**•** Notification is not already marked as seen |
| **Postconditions** | **•** Notification is updated with `seen = true`<br>**•** Update is logged |
| **Main Flow** | 1. Receive request to make notification seen<br>2. Validate id is not null<br>3. Find notification by ID<br>4. Check if notification is already seen<br>5. Update notification to `seen = true`<br>6. Log through `bc-audit-log` |
| **Alternative Flows** | 2a. ID is null → `ValidationException`<br>3a. Notification not found → `NotFoundException`<br>5a. Persistence error → `PersistenceError`<br>6a. Audit log fails → `LogError` |
| **Priority** | Medium |

<br>

## API Endpoints

**POST /internal/notifications/create**<br>
Create a notification

**GET /internal/notifications/{uuid}**<br>
Retrieve a single notification by its id

**PUT /internal/notifications/{uuid}/seen**<br>
Mark a notification as seen

**DELETE /internal/notifications/{uuid}**<br>
Deletes a notification

**PUT /internal/notifications/{uuid}/compensate/create**<br>
Compensates a notification

## Dependencies

### Internal Dependencies
- application.properties
- pom.xml

### External Dependencies
- Docker
- Database: Mariadb

## Contributing Guidelines

### Code Standards

**Naming Conventions**
- **Classes & Interfaces**: PascalCase (e.g., `NotificationDirectory`, `NotificationApplicationService`)
- **Methods**: camelCase (e.g., `createNotification`, `markAsSeen`)
- **Variables & Parameters**: camelCase (e.g., `notificationId`, `personRef`)
- **Constants & Enums**: UPPER_SNAKE_CASE (e.g., `MAX_NOTIFICATION_COUNT`, `NOTIFICATION_TYPE_EMAIL`)
- **Folders**: kebab-case (e.g., `notification-api/`, `notification-impl/`)
- **Files**: PascalCase for Java files (e.g., `NotificationDirectory.java`)

## Configuration

### Envoriment Variables
Handled by the pipeline

### Dockerfile
Dockerfile makes the microservice into a .jar file that can be used by our handlers.