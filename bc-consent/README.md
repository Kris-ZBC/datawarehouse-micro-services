# BC-Consent

## Purpose
`bc-consent` is the bounded context responsible for managing consent and consent statements within the system.  
This context handles the lifecycle of consent from granting to withdrawal and ensures that consent information can be retrieved and maintained consistently.

The consent bounded context integrates with other bounded contexts in order to support auditing and anonymization of data when consent is withdrawn.

## Functional scope
The consent BC contains support for:
- Granting consent
- Retrieving consent
- Withdrawing consent
- Updating consent statement
- Retrieving consent statement
- Creating audit-log entries through integration with `bc-audit_log`
- Triggering anonymization through `bc-anonymize` when consent is withdrawn

## Module layout
The BC is split into two Maven modules:

- `consent-api`
	- API contracts and shared DTO/domain contract types (commands, queries and responses)

- `consent-impl`
	- Spring Boot implementation (application/service/adapters), persistence, migrations and runtime configuration

## Architecture style
This BC follows onion and clean architecture principles:

- **Domain** contains the core repository port used by the application
- **Application** orchestrates use-case flows through the `ConsentApplicationService`
- **Interface adapters** handle persistence through JPA adapters
- **Infrastructure/resources** contain technical concerns such as DB migration and runtime configuration

## Integration and technical boundaries

- **Audit logging**: business-relevant actions can be logged through `bc-audit_log`
- **Anonymization**: when consent is withdrawn the handler in `bc-anonymize` can be triggered to anonymize data
- **Database**: Flyway migration is located in `consent-impl/src/main/resources/db/migration`
- **Runtime**: service is configured as a Spring Boot app (`consent-impl`) with port `9445` as default

---

# Use Cases

| UC-0001 | Grant consent |
|---------|----------------|
| **Description** | Grants consent for a person for a given purpose |
| **Actor** | Person |
| **Trigger** | `GrantConsentCmd` received |
| **Preconditions** | **•** Person reference exists<br>**•** Consent statement exists<br>**•** Consent is not already granted |
| **Domain Rules** | **•** A person may only have one active consent for the same purpose |
| **Postconditions** | **•** Consent is stored as granted<br>**•** Audit-log entry is created through `bc-audit_log` |
| **Main Flow** | 1. Receive `GrantConsentCmd`<br>2. Validate consent statement<br>3. Validate person reference<br>4. Check whether consent already exists<br>5. Persist consent<br>6. Create audit-log entry through `bc-audit_log`<br>7. Return `GrantedConsentResponse` |
| **Alternative Flows** | 3a. Consent statement not found → `ConsentStatementNotFoundException`<br>4a. Consent already granted → `ConsentAlreadyGrantedException`<br>5a. Persistence failure → `PersistenceError`<br>6a. Audit log fails → `LogError` |
| **Priority** | Critical |

<br>

| UC-0002 | Retrieve consent from a person |
|---------|----------------|
| **Description** | Retrieves consent information for a specific person |
| **Actor** | System |
| **Trigger** | `GetConsentQuery` received |
| **Preconditions** | **•** Consent for the person reference exists |
| **Domain Rules** | **•** Only existing consent records can be retrieved |
| **Postconditions** | **•** Consent information is returned |
| **Main Flow** | 1. Receive `GetConsentQuery`<br>2. Retrieve consent from repository using person reference<br>3. Return `ConsentResponse` |
| **Alternative Flows** | 2a. Consent not found → `ConsentNotFoundException` |
| **Priority** | High |

<br>

| UC-0003 | Revoke consent |
|---------|----------------|
| **Description** | Revokes an existing consent for a person |
| **Actor** | Person |
| **Trigger** | `WithdrawConsentCmd` received |
| **Preconditions** | **•** Consent for person reference exists<br>**•** Consent for person reference is currently active |
| **Domain Rules** | **•** Only active consent can be revoked |
| **Postconditions** | **•** Consent is marked as revoked<br>**•** Audit-log entry is created through `bc-audit_log`<br>**•** Anonymization is triggered through `bc-anonymize` |
| **Main Flow** | 1. Receive `WithdrawConsentCmd`<br>2. Retrieve consent<br>3. Validate consent is active<br>4. Update consent status to revoked<br>5. Persist updated consent<br>6. Create audit-log entry through `bc-audit_log`<br>7. Trigger anonymization handler through `bc-anonymize`<br>8. Return `WithdrawnConsentResponse` |
| **Alternative Flows** | 2a. Consent not found → `ConsentNotFoundException`<br>3a. Consent already revoked → `InvalidConsentStateException`<br>5a. Persistence update failed → `PersistenceError`<br>6a. Audit log fails → `LogError`<br>7a. Anonymization handler fails → `AnonymizationError` |
| **Priority** | Critical |

<br>

| UC-0004 | Update consent statement |
|---------|----------------|
| **Description** | Updates an existing consent statement |
| **Actor** | System / Administrator |
| **Trigger** | `UpdateConsentStatementCmd` received |
| **Preconditions** | **•** Consent statement exists |
| **Domain Rules** | **•** Only existing consent statements can be updated |
| **Postconditions** | **•** Consent statement is updated<br>**•** Audit-log entry is created through `bc-audit_log` |
| **Main Flow** | 1. Receive `UpdateConsentStatementCmd`<br>2. Retrieve consent statement<br>3. Validate it exists<br>4. Update consent statement<br>5. Persist updated statement<br>6. Create audit-log entry through `bc-audit_log`<br>7. Return `UpdatedConsentStatementResponse` |
| **Alternative Flows** | 2a. Consent statement not found → `ConsentStatementNotFoundException`<br>5a. Persistence failure → `PersistenceError`<br>6a. Audit log fails → `LogError` |
| **Priority** | Medium |

<br>

| UC-0005 | Retrieve consent statement |
|---------|----------------|
| **Description** | Retrieves a consent statement |
| **Actor** | System |
| **Trigger** | `GetConsentStatementQuery` received |
| **Preconditions** | **•** Consent statement exists |
| **Domain Rules** | **•** Only existing consent statements can be retrieved |
| **Postconditions** | **•** Consent statement information is returned |
| **Main Flow** | 1. Receive `GetConsentStatementQuery`<br>2. Retrieve consent statement from repository<br>3. Return `ConsentStatementResponse` |
| **Alternative Flows** | 2a. Consent statement not found → `ConsentStatementNotFoundException` |
| **Priority** | High |
---

## API Endpoints
- **GET**
  - /consents/{id}
  - /consent-statements/{id}

- **POST**
  - /consents/grant
  - /consents/withdraw

- **PUT**
  - /consent-statements/{id}

- **DELETE**
  - /something

---

## Dependencies

### Internal Dependencies
- application.yml
- pom.xml

### External Dependencies
- Docker
- Database: MariaDB
- Spring Boot
- Spring Data JPA
- Flyway

---

## Contributing Guidelines

### Code Standards

**Naming Conventions**

- **Classes**: PascalCase (e.g., `ConsentApplicationService`, `ConsentRepositoryPort`)
- **Methods**: camelCase (e.g., `grantConsent`, `withdrawConsent`)
- **Variables**: camelCase (e.g., `consentStatus`, `subjectReference`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_CONSENT_DURATION`)
- **Files**: kebab-case for folders, PascalCase for class files (e.g., `consent-api/`, `ConsentApplicationService.java`)

---

## Configuration

### Environment Variables
Handled by the pipeline

### Dockerfile
Dockerfile makes the microservice into a .jar file that can be used by our handlers.

---

<br>
*Last Updated: 12-03-2026*  
*Version* 1.0.0