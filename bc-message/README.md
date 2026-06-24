# Message

## Purpose
bc-message is the bounded context responsible for creating and retrieving messages sent from one person to one or more persons.

## Current functional scope
Implemented in this BC today:

- Create a message from one sender to one or more recipient persons
- Retrieve the latest messages for a given recipient person with a limit

Not implemented in this BC today:

- Update message
- (Delete message)
- Audit-log integration

## Module layout
This BC contains two Maven modules:

- message-api
	- Public application contract (`MessageDirectory`)
	- Request and response DTOs
- message-impl
	- Spring Boot runtime module
	- Domain, application service, web adapter, and persistence adapters
	- Flyway migration and i18n resources

## Architecture
The implementation follows a layered onion/hexagonal style architecture:

- Domain
	- `Message`: aggregate root for message data and validation
	- `MessageId`: value object for the message identifier
	- `MessageDomain` / `MessageDomainService`: domain creation logic
	- `MessageRepositoryPort`: outbound persistence contract
- Application
	- `MessageApplicationService`: use case orchestration and validation of application level input
	- `MessageResponseMapper`: maps domain model to API response DTO
- Interface adapters
	- Persistence (`interfaceadapters/persistence/jpa/`)
		- `MessageSpringDataRepository`
		- `MessageRepositoryJpaAdapter`
		- `MessageEntity`
		- `MessageJpaMapper`
	- Web (`interfaceweb/`)
		- `InternalMessageController`

## Domain model
A message contains:

- `id` (`UUID`)
- `dateTimeSent` (`OffsetDateTime`)
- `message` (`String`)
- `senderPersonRef` (`UUID`)

### Domain rules currently enforced
The domain currently validates that:

- message id must be present
- sent timestamp must be present
- sender person reference must be present
- message text must not be blank
- message text must not exceed 4000 characters
- recipient list must be present
- recipient list must contain at least one recipient
- recipient list must not contain null values

## API endpoints
Base path: `/internal/messages`

- `POST /`
	- Create a message for one or more recipient persons
	- Request body: `CreateMessageCmd`
	- Response: `MessageResponse`

- `GET /person/{personRef}?limit=10`
	- Retrieve latest messages where the given person is a recipient
	- Path variable: `personRef` (`UUID`)
	- Query parameter: `limit` (`int`, default `10`, max `100`)
	- Response: `List<MessageResponse>`



## Contracts and data

### CreateMessageCmd
Fields:

- `senderPersonRef` (`UUID`)
- `message` (`String`)

Validation annotations on the DTO:

- `senderPersonRef` must be present
- each recipient UUID must be non-null
- `message` must not be blank
- `message` max length is 4000

### MessageResponse
Fields:

- `id` (`UUID`)
- `dateTimeSent` (`OffsetDateTime`)
- `message` (`String`)
- `senderPersonRef` (`UUID`)

### UpdateMessageCmd
`UpdateMessageCmd` exists in the API module but is currently not part of the functional scope and is not used by the implementation.

## Persistence
JPA tables:

- `message`
- `message_recipient`

Main persistence components:

- `MessageEntity`
- `MessageSpringDataRepository`
- `MessageJpaMapper`
- `MessageRepositoryJpaAdapter`

Migration:

- `message-impl/src/main/resources/db/migration/V1__init.sql`

### Persistence design note
Recipients are currently stored as an `@ElementCollection` of UUID references in `message_recipient`.

This is a good fit for the current use case because recipients only need to exist as references. If recipient-specific state is needed later, such as:

- delivery status
- read timestamp
- per-recipient audit data

then `message_recipient` should be promoted to a dedicated entity instead of remaining an element collection.

## i18n
Validation and error messages are backed by resource bundles:

- `message-impl/src/main/resources/i18n/message-messages.properties`
- `message-impl/src/main/resources/i18n/message-messages_da.properties`

Examples of message keys used by this BC:

- `message.id.required`
- `message.dateTimeSent.required`
- `message.senderPersonRef.required`
- `message.message.required`
- `message.message.maxLength`
- `message.create.required`
- `message.personRef.required`
- `message.limit.invalid`
- `message.limit.max`

## Testing notes
The module currently contains tests for:

- domain model validation (`MessageTest`)
- domain service creation (`MessageDomainServiceTest`)
- application service behavior (`MessageApplicationServiceTest`)
- response mapping (`MessageResponseMapperTest`)
- JPA mapping (`MessageJpaMappingTest`)
- JPA adapter behavior (`MessageRepositoryJpaAdapterTest`)
- controller delegation (`InternalMessageControllerTest`)
- architecture sanity and onion structure checks
	- `ArchPropsSanityTest`
	- `OnionArchitectureTest`

### Testing strategy
Testing in this BC is split across the actual responsibilities of the implementation:

- domain tests verify business validation close to the aggregate
- application tests verify use case orchestration and input guards
- persistence tests verify repository adapter and mapping behavior
- controller tests verify delegation to the application contract
- architecture tests verify the intended project structure

This keeps tests aligned with the architecture instead of only testing through Spring integration.

## Configuration
Main runtime properties are in:

- `message-impl/src/main/resources/application.properties`

Current defaults include:

- `bc.qualifier=message`
- `server.port=9449`
- `spring.profiles.active` is inherited from the runtime environment / parent configuration
- `security.enabled=true`
- `server.ssl.enabled=true`
- `internal.mtls.allowed-callers=registration-saga`

Additional runtime configuration includes:

- TLS keystore location through Spring SSL bundle config
- virtual thread support
- SOPINFO logging auto-configuration

## Dependencies
Internal module dependencies:

- `message-api`
- `common-core`
- `common-data`
- `common-security`
- `common-web`

Additional current internal dependency:

- `person-notification-api`

External technology dependencies:

- Spring Boot (web, data-jpa, security, validation, actuator)
- Flyway
- MariaDB JDBC driver

## Build and test
From repository root:

- Run tests for this module:
	- `mvn -pl ./bc-message/message-impl -am test`

- Run tests with coverage:
	- `mvn -pl :message-impl -am clean verify`

- Build this module:
	- `mvn -pl ./bc-message/message-impl -am package`

- Run this microservice:
	- `mvn -pl :message-impl -am spring-boot:run`

## Future extension points
Likely next extensions for this BC:

- audit-log integration after create
- dedicated education-line based creation flow
- stronger recipient rules such as duplicate detection or blocking sender as recipient
- richer querying/filtering
- per-recipient message state if delivery/read tracking becomes necessary