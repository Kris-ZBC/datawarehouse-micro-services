# BC-Login

## Purpose
`bc-login` is the bounded context responsible for managing user authentication, and login credentials within the system. This context handles the complete lifecycle of user login accounts, from creation and activation to deactivation, including all authentication operations and password management. It serves as the central authentication hub and integrates with the `bc-audit_log` bounded context to maintain comprehensive security audit trails.

## Functional scope
The login BC contains support for:
- Creating a new login
- Authenticating by validating credentials
- Changing password if default
- Reset password to default
- Logging out (terminating sessions)
- Activating login
- Deactivating login
- Creating audit-log entries through integration with `bc-audit_log`  **With a helper / SAGA**

## Module layout
The BC is slit into two Maven modules:
- `login-api`
	- API contracts and shared DTO/domain contract types
- `login-impl`
	- Spring Boot implementation (application/domain/adapters), persistence, migrations, i18n, and runtime configuration

## Architecture style
This BC follow onion and clean architecture principles:
- **Domain** is central and does not depend on infrastructure
- **Application** orchestrates use-case flows
- **Interface adapters** handle inbound/outbound integration (web, persistence, messaging)
- **Infrastructure/resources** contain technical concerns such as DB migration and i18n

## Integration and technical boundaries

- **Audit logging**: business-relevant actions can be logged through `bc-audit_log` **With a helper / SAGA**
- **Database**: Flyway migration is located in `login-impl/src/main/resources/db/migration`
- **Localization**: message bundles are in `login-impl/src/main/resources/i18n`
- **Runtime**: service is configured as a Spring Boot app (`login-impl`) with port `not chosen` as default

## Use Cases

| UC-0001 | Create a login |
|---------|----------------|
| **Description** | Creates a new login with default password for an already existing person |
| **Actor** | Person |
| **Trigger** | `CreateLoginCommand` received |
| **Preconditions** | **•** Actor is authenticated<br>**•** Person reference is validated<br>**•** Login does not already exists for the person<br>**•** Default password exists |
| **Domain Rules** | **•** A person may only have one login<br>**•** Passwords must never be stored in plaintext<br>**•** New logins require password change on first authentication |
| **Postconditions** | **•** New login is created for person reference<br>**•** Creation event is logged using AuditLog bounded context<br>**•** Login is intially active but requires password change |
| **Main Flow** | 1. Receive `CreateLoginCommand` with person reference<br>2. Check if login already exists for person reference<br>3. Receive default password<br>4. Hash login entity and store it<br>5. Set login as active<br>6. Log `LoginCreatedEvent` using AuditLog bounded context<br>7. Return `LoginCreatedResponse` |
| **Alternative Flows** | 2a. Login already exists → `LoginExistsError`<br>3a. Default password doesn't exist → `DefaultPasswordNotFound`<br>4a Persistence error → `PersistenceError`<br>6a. Audit log fails → `LogError` |
| **Priority** | Critical |

<br>

| UC-0002 | Authenticate person by validating credentials |
|---------|----------------|
| **Description** | Validates credentials and authenticates the person |
| **Actor** | Person |
| **Trigger** | `AuthenticateCommand` received |
| **Preconditions** | **•** Login exists for person<br>**•** Login status is active |
| **Domain Rules** | **•** Deactivated logins cannot be authenticated |
| **Postconditions** | **•** Person has session with authentication token<br>Authentication event is logged using AuditLog bounded context |
| **Main Flow** | 1. Receive `AuthenticateCommand` with credentials<br>2. Retrieve hashed login entity by username<br>3. Validate login is activated<br>4. Validate hashed password<br>5. Generate authentication token<br>6. Create user session entity<br>7. Log `AuthenticationSucceededEvent` using AuditLog bounded context<br>8. Return `AuthenticationResponse` with token |
| **Alternative Flows** | 2a. Login entity not found → `AuthenticationFailedException`<br> 3a. Login is not active → `AccountDeactivatedException` <br>4a. Invalid password → `AuthenticationFailedException`<br>5a. Token generation fails → `TokenGenFailedException`<br>6a. Couldn't establish session → `SessionError`<br>7a. Audit log fails → `LogError`|
| **Priority** | Critical |

| UC-0003 | Change password if password is default |
|---------|----------------|
| **Description** | Allows person to change default password when password has been reset to default |
| **Actor** | Person |
| **Trigger** | `ChangePasswordCommand` received |
| **Preconditions** | **•** Person is authenticated<br>**•** Their password is default password |
| **Domain Rules** | **•** New password must follow password policy<br>**•** Password must be stored as secure hash |
| **Postconditions** | **•** Password is changed from the default password<br>**•** Password changed event is logged using AuditLog bounded context<br>**•** Person's session continues with new authentication |
| **Main Flow** | 1. Receive `ChangePasswordCommand` with new password<br>2. Validate new password against password policy<br>3. Hash new password<br>4. Update login entity with new password<br><br>5. Log `PasswordChangedEvent` using AuditLog bounded context<br>6. Return `PasswordChangeSuccessResponse`|
| **Alternative Flows** | 2a. Password violates policy → `PasswordPolicyViolationException`<br>4a. Persistence update failed → `PersistenceUpdateException`<br>5a. Audit log fails → `LogError` |
| **Priority** | High |

| UC-0004 | Reset password to default |
|---------|----------------|
| **Description** | Resets a person's password to a default password |
| **Actor** | Person |
| **Trigger** | `ResetPasswordCommand` received |
| **Preconditions** | **•** Person is authenticated<br>**•** Login exists for person reference |
| **Domain Rules** | **•** Resetting the password terminates all existing sessions for that login |
| **Postconditions** | **•** Password is now the default password<br>**•** Person is authenticated<br>**•**Password reset event is logged using AuditlLog bounded context<br> **•** Log person out of system |
| **Main Flow** | 1. Receive `ResetPasswordCommand`<br>2. Check if login exists<br>3. Check if login is active<br>4. Fetch default password<br>5. Update login entity with new default password<br>6. Log user out of system<br>7. Log `PasswordResetEvent` using AuditLog bounded context<br>8. Return `PasswordResetSuccessResponse`|
| **Alternative Flows** | 2a. Login doesn't exist → `LoginNotFound`<br>3a. Login isn't active → `LoginNotActive`<br>4a. Couldn't find default password → `DefaultPasswordNotFound`<br>5a. Persistence update failed → `PersistenceUpdateException`<br>6a. Failed to log user out → `LogOutException`<br>7a. Audit log fails → `LogError` |
| **Priority** | Medium |

| UC-0005 | Logout Person |
|---------|----------------|
| **Description** | Terminates an active session for a logged-in person |
| **Actor** | Person |
| **Trigger** | `LogoutCommand` received |
| **Preconditions** | **•** Actor is authenticated<br>**•** Session exists<br>**•** Session is active |
| **Domain Rules** | **•** Only active sessions can be terminated<br>**•** Terminated sessions cannot be used |
| **Postconditions** | **•** Session is terminated<br>**•** Session can no longer be used for authentication |
| **Main Flow** | 1. Received `LogoutCommand`<br>2. Verify session exists<br>3. Terminate session (delete)<br>4. Log `PersonLoggedOutEvent` using AuditLog bounded context<br>5. Return `LogoutResponse` |
| **Alternative Flows** | 2a. Session not found → `SessionNotFound`<br>4a. Audit log fails → `LogError` |
| **Priority** | High |

| UC-0006 | Activate login |
|---------|----------------|
| **Description** | Activates a login so that the login can be used for authentication |
| **Actor** | Person |
| **Trigger** | `ActivateLoginCommand` received |
| **Preconditions** | **•** Actor is authenticated<br>**•** Login exists for person reference |
| **Domain Rules** | **•** Only deactivated logins can be activated |
| **Postconditions** | **•** Login status is set to active<br>**•** Login can now be used for authentication |
| **Main Flow** | 1. Receive `ActivateLoginCommand`<br>2. Verify login exists<br>3. Verify login status is deactive<br>4. Set login status to active<br>5. Log `LoginActivatedEvent` using AuditLog bounded context<br>6. Return `LoginActivatedResponse` |
| **Alternative Flows** | 2a. Login not found → `LoginNotFound`<br>3a. Login is active → `LoginIsActiveException`<br>4a. Persistence failure → `PersistenceFailException`<br>5a. Audit log fails → `LogError` |
| **Priority** | Medium |

| UC-0007 | Deactivate login |
|---------|----------------|
| **Description** | Deactivates a login so that the login cannot be used for authentication |
| **Actor** | Person |
| **Trigger** | `DeactivateLoginCommand` received |
| **Preconditions** | **•** Actor is authentication<br>**•** Login exists for person reference |
| **Domain Rules** | **•** Only activated logins can be deactivated |
| **Postconditions** | **•** Login status is set to deactive<br>**•** Login can not be used for authentication<br> **•** All sessions is terminated |
| **Main Flow** | 1. Receive `DeactiveLoginCommand`<br>2. Verify login exists<br>3. Verify login status is active <br>4. Set login status to deactive<br>5. Terminate all active sessions<br>6. Log `LoginDeactivatedEvent` using AuditLog bounded context<br>7. Return `LoginDeactivatedResponse` |
| **Alternative Flows** | 2a. Login not found → `LoginNotFound`<br>3a. Login is deactive → `LoginIsDeactiveException`<br>4a. Persistence failure → `PersistenceFailException`<br>5a. Couldn't terminate sessions → `TerminateSessionsException`<br>6a. Audit log fails → `LogError` |
| **Priority** | Medium |

## API Endpoints

- **POST**
  - `/internal/logins` — Create a new login
  - `/internal/logins/sessions/login` — Authenticate (sets session cookie)
  - `/internal/logins/sessions/logout` — Logout (terminate session)
  - `/internal/logins/compensate` — SAGA compensate

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
- **Classes**: lowercase (e.g., `apiarchitecturetest`, `logindirectory`)
- **Methods**: camelCase (e.g., `resetPassword`, `createUser`)
- **Variables**: camelCase (e.g., `isActivated`, `userCredentials`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_LOGIN_ATTEMPTS`, `PASSWORD_LENGTH`)
- **Files**: kebab-case for folders, lowercase for class files (e.g., `login-api/`, `logindirectory.java`)
- **i18n message keys**: lowercase dot-separated (e.g., `login.personref.invalid`, `session.notfound`)

## Configuration

### Environment Variables
Handled by the pipeline

### Dockerfile
Dockerfile packages the pre-built JAR (produced by Maven) into a container image. The build stage is handled by the pipeline, not Docker.

---

<br>
*Last Updated: 26-05-2026*<br>
*Version* 1.0.0