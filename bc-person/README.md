# Person Service (bc-person)

Internal service responsible for managing **Person** data and exposing a stable reference (`personId` as UUID) that other bounded contexts (e.g. **bc-apprentice**) can store and use.

## Scope
This service is the **source of truth** for:
- First name
- Last name
- Email
- Phone number

Each person can have **multiple phone numbers**.
Each phone number has a 'PhoneUserType' that describes who the number belongs to.

Other services must reference a person by **UUID** and must not copy-write Person data.

---

## Use cases

### Create
- Can create a person with the given information.

### Update
- Can update first name
- Can update last name
- Can update email
- Can update phone number

### Retrieve
- Can retrieve first name
- Can retrieve last name
- Can retrieve email
- Can retrieve phone number

---

## Domain model

Person
- personId (UUID)
- firstName
- lastName
- email
- organizationRef (UUID)

PhoneNumber
- phoneNumber
- phoneUserType

Relationship:
- A person can have **multiple phone numbers**.

## Public contract

### Identifiers
- `personId`: UUID (string in JSON)

### Enums

#### PhoneUserType
Defines who the stored phone number belongs to.

Possible values:
- `SELF`
- `PARENT`
- `GUARDIAN`
- `EMERGENCY_CONTACT`
- `OTHER`

## API (internal)

Base path:
- `/internal/persons`

## i18n

This bounded context uses:
- `person-messages.properties`
- `person-messages_da.properties`


## Phone number handling

`PhoneNumberValue` validates and normalizes phone numbers in the domain.

Rules:
- the value must not be blank
- the value must contain digits
- the number must contain between 7 and 15 digits
- a leading `+` is preserved if present at the start of the input
- all other non-digit characters are removed during normalization

Examples:
- `+45 12 34 56 78` -> `+4512345678`
- `12-34-56-78` -> `12345678`
- `( +45 ) 12 34 56 78` is not treated as a leading `+` unless `+` is the first character


## Endpoints

Base path:

`/internal/persons`

### Create person

**POST** `/internal/persons`

Request body:

```json
{
  "firstName": "Jakob",
  "lastName": "Hansen",
  "email": "jakob.hansen@example.com",
  "organizationRef": "22222222-2222-2222-2222-222222222222",
  "phoneNumbers": [
    {
      "type": "SELF",
      "value": "12345678"
    }
  ]
}