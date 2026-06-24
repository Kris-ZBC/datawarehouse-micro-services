# Apprentice Service (bc-apprentice)

Internal service responsible for managing **Apprentice** data. An apprentice is a simple link between a **person** and an **education line**, providing a stable UUID (`apprenticeId`) that other bounded contexts can store and reference.

## Scope
This service is the **source of truth** for:

- `apprenticeId` (UUID)
- `personRef` (UUID referencing bc-person)
- `educationLineRef` (UUID referencing bc-education_line)

An apprentice cannot exist without valid person and education references. Other services should only reference apprentices by their UUID and must not duplicate apprentice data.

---

## Domain rules

- An apprentice represents a relationship between **one person and one education line**.  
- A **person can only have one apprentice record** (`personRef` is unique).  
- Multiple apprentices may reference the **same education line**.  
- An apprentice **cannot exist without valid references** to a person and an education line.  
- The apprentice record is **immutable after creation**.

---

## Use cases

### Create
- Create an apprentice given a `personRef` and an `educationLineRef`.

### Retrieve
- Fetch an apprentice by its `apprenticeId`.
- (Potential future enhancements may allow listing by person or education line.)

### Update
- Currently the domain model is immutable once created. Updates would require a new record or explicit domain logic.

---

## Domain model

**Apprentice**
- `apprenticeId` (UUID)
- `personRef` (UUID)
- `educationLineRef` (UUID)

Value objects used internally:

- `ApprenticeId`
- `PersonRef`
- `EducationLineRef`

---

## Public contract

### Identifiers
- `apprenticeId`: UUID (string in JSON)

### DTOs
- `CreateApprenticeCmd`
- `CreatedApprenticeResponse`
- `ApprenticeResponse`

### Directory
- `ApprenticeDirectory`

### Enums
None currently.

---

## API (internal)

Base path:

- `/internal/apprentices` (implementation may be in `apprentice-api` module)

Typical operations:

- `POST /internal/apprentices` – create an apprentice.
- `GET /internal/apprentices/{apprenticeId}` – retrieve by id.

Example create request:

```json
{
  "personRef": "uuid",
  "educationLineRef": "uuid"
}