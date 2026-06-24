# Education Line Saga

## Purpose
The education-line-saga orchestrates cross-service operations for education line use cases.

Its role is to:

- Execute multi-step application actions in a controlled order
- Verify each critical step before continuing
- Create audit log entries for completed actions
- Trigger compensating actions when a downstream step fails

The goal is to keep business behavior consistent when operations span multiple services.

## What this saga does today
Implemented in this saga:

- Create education line (with education reference validation)
- Update education line name
- Update education line duration
- Deactivate education line
- Activate education line

For each use case, this saga coordinates business action + verification + audit logging, and compensates when required.

## How the flow works
The saga is implemented as an orchestrating application service that calls downstream services through HTTP ports.

General execution pattern:

1. Validate prerequisites if needed
2. Execute the primary action in `bc-education-line`
3. Verify the result by reading the updated resource
4. Create and verify an audit log entry
5. Return success response

If any verification or audit step fails, the saga runs a compensation action for the affected business step (and compensates audit log if it was already created).

## Current orchestration behavior by use case

- **Create**
  - Validates that `educationRef` exists via `bc-education`
  - Creates the education line in `bc-education-line`
  - Verifies created resource can be read
  - Creates and verifies audit log entry
  - Compensates education line creation if later steps fail

- **Update name**
  - Reads previous name
  - Updates the name in `bc-education-line`
  - Verifies updated name
  - Creates and verifies audit log entry
  - Compensates by restoring previous name if a later step fails

- **Update duration**
  - Reads previous duration values
  - Updates the duration in `bc-education-line`
  - Verifies updated duration
  - Creates and verifies audit log entry
  - Compensates by restoring previous duration if a later step fails

- **Deactivate / Activate**
  - Executes the status change in `bc-education-line`
  - Verifies resulting active status
  - Creates and verifies audit log entry
  - Compensates status change if a later step fails

## Module layout
This saga contains two Maven modules:

- `education-line-saga-api`
  - Public application contract (`EducationLineSagaDirectory`)
  - Request and response DTOs
- `education-line-saga-impl`
  - Spring Boot runtime module
  - Orchestration service and HTTP adapters
  - Web controller and configuration

## API endpoints
Base path: `/internal/saga/educationlines`

- `POST /create`
  - Body: `CreateEducationLineCmd`
- `PUT /{id}/name`
  - Body: `UpdateEducationLineNameCmd`
- `PUT /{id}/duration`
  - Body: `UpdateEducationLineDurationCmd`
- `PUT /{id}/deactivate`
  - Body: `CreateAuditlogCmd`
- `PUT /{id}/activate`
  - Body: `CreateAuditlogCmd`

## Downstream dependencies
This saga currently calls these services:

- `bc-education` (education existence check)
- `bc-education-line` (business operations + compensations)
- `bc-audit-log` (audit creation, verification, compensation)

## Notes

- This saga is request-driven orchestration with compensating actions.
- Current implementation does not describe a persisted saga state machine in this module.