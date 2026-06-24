# Education Saga

## Purpose

The education-saga orchestrates cross-service operations for education use cases.

Its role is to:

* Execute multi-step application actions in a controlled order
* Verify each critical step before continuing
* Create audit log entries for completed actions
* Trigger compensating actions when a downstream step fails
* Prevent concurrent execution of sensitive operations through saga locking

The goal is to keep business behavior consistent when operations span multiple services.

## What this saga does today

Implemented in this saga:

* Create education
* Update education name
* Update education category
* Activate education
* Deactivate education
* Create education instructor connection
* Activate education instructor connection
* Deactivate education instructor connection

For each use case, this saga coordinates business action + verification + audit logging, and compensates when required.

## How the flow works

The saga is implemented as an orchestrating application service that calls downstream services through HTTP ports.

General execution pattern:

1. Acquire saga lock when required
2. Validate prerequisites if needed
3. Execute the primary action in the target business service
4. Verify the result by reading the updated resource
5. Create and verify an audit log entry
6. Return success response

If any verification or audit step fails, the saga runs a compensation action for the affected business step (and compensates audit log if it was already created).

## Current orchestration behavior by use case

* **Create education**

  * Acquires a saga lock using the session identifier
  * Creates the education in `bc-education`
  * Verifies created resource can be read
  * Creates and verifies audit log entry
  * Compensates education creation if later steps fail

* **Update name**

  * Updates the name in `bc-education`
  * Verifies updated name
  * Creates and verifies audit log entry
  * Compensates by restoring previous value if a later step fails

* **Update category**

  * Updates the category in `bc-education`
  * Verifies updated category
  * Creates and verifies audit log entry
  * Compensates by restoring previous value if a later step fails

* **Deactivate / Activate education**

  * Executes the status change in `bc-education`
  * Verifies resulting active status
  * Creates and verifies audit log entry
  * Compensates status change if a later step fails

* **Create education instructor**

  * Creates the education-instructor connection in `bc-education-instructor`
  * Verifies created resource can be read
  * Creates and verifies audit log entry
  * Compensates creation if later steps fail

* **Deactivate / Activate education instructor**

  * Executes the status change in `bc-education-instructor`
  * Verifies resulting active status
  * Creates and verifies audit log entry
  * Compensates status change if a later step fails

## Module layout

This saga contains two Maven modules:

* `education-saga-api`

  * Public application contract (`EducationSagaDirectory`)
  * Request and response DTOs

* `education-saga-impl`

  * Spring Boot runtime module
  * Orchestration service and HTTP adapters
  * Web controller and configuration

## API endpoints

Base path: `/internal/saga/educations`

* `POST /create`

  * Body: `CreateEducationCmd`

* `PUT /{id}/name`

  * Body: `UpdateEducationCmd`

* `PUT /{id}/category`

  * Body: `UpdateEducationCmd`

* `PUT /{id}/activate`

  * Body: `CreateAuditlog`

* `PUT /{id}/deactivate`

  * Body: `CreateAuditlog`

* `POST /education/instructor/create`

  * Body: `CreateEducationInstructorCmd`

* `PUT /education-ref/{educationId}/instructor-ref/{instructorId}/activate`

  * Body: `CreateAuditlog`

* `PUT /education-ref/{educationId}/instructor-ref/{instructorId}/deactivate`

  * Body: `CreateAuditlog`

## Downstream dependencies

This saga currently calls these services:

* `bc-education` (education operations and compensations)
* `bc-instructor` (instructor existence validation)
* `bc-education-instructor` (education-instructor operations and compensations)
* `bc-audit-log` (audit creation, verification, compensation)

## Notes

* This saga is request-driven orchestration with compensating actions.
* Education creation uses a saga lock to prevent concurrent processing for the same session.
* Current implementation does not describe a persisted saga state machine in this module.
